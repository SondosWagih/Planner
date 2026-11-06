package app.planner

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudyFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private fun str(id: Int) = rule.activity.getString(id)

    private fun openTab(label: Int) {
        rule.onNode(hasText(str(label)) and hasClickAction()).performClick()
    }

    private fun waitForDialogToClose() {
        rule.waitUntil(5_000) { rule.onAllNodesWithText(str(R.string.save)).fetchSemanticsNodes().isEmpty() }
    }

    @Test fun addCourseThenClassShowsACardOnTheTimetable() {
        val name = "Course ${System.nanoTime()}"

        openTab(R.string.tab_courses)
        rule.onNodeWithContentDescription(str(R.string.add)).performClick()
        rule.onNodeWithText(str(R.string.field_name)).performTextInput(name)
        rule.onNodeWithText(str(R.string.save)).performClick()
        waitForDialogToClose()
        rule.waitUntil(5_000) { rule.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty() }

        openTab(R.string.tab_schedule)
        rule.waitUntil(5_000) { rule.onAllNodesWithContentDescription(str(R.string.add)).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithContentDescription(str(R.string.add)).performClick()
        rule.onNodeWithText(name).performClick()          // pick the new course in the form
        rule.onNodeWithText(str(R.string.save)).performClick()
        waitForDialogToClose()

        rule.waitUntil(5_000) { rule.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty() }
        rule.onAllNodesWithTag("event_card").onFirst().assertExists()
    }
}
