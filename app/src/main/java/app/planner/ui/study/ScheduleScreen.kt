package app.planner.ui.study

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.planner.R
import app.planner.data.local.entity.StudyEventEntity

private sealed interface ScheduleDialog {
    data class Edit(val item: StudyEventEntity?) : ScheduleDialog
}

@Composable
fun ScheduleScreen(viewModel: StudyViewModel, modifier: Modifier = Modifier) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf<ScheduleDialog?>(null) }
    var deleting by remember { mutableStateOf<StudyEventEntity?>(null) }
    val courseById = remember(ui.courses) { ui.courses.associateBy { it.id } }

    if (ui.courses.isEmpty()) {
        CenteredMessage("screen_schedule", stringResource(R.string.add_course_first))
    } else {
        Box(modifier.fillMaxSize().testTag("screen_schedule")) {
            WeeklyTimetable(
                events = ui.events,
                courses = courseById,
                onEventClick = { dialog = ScheduleDialog.Edit(it) },
                modifier = Modifier.fillMaxSize(),
            )
            AddButton({ dialog = ScheduleDialog.Edit(null) }, Modifier.align(Alignment.BottomEnd))
        }
    }

    when (val d = dialog) {
        null -> Unit
        is ScheduleDialog.Edit -> EventForm(
            initial = d.item,
            courses = ui.courses,
            onDismiss = { dialog = null },
            onSave = viewModel::saveEvent,
            onDelete = d.item?.let { e -> { dialog = null; deleting = e } },
        )
    }
    deleting?.let { e ->
        DeleteDialog(R.string.delete_item_msg, onConfirm = { viewModel.deleteEvent(e.id) }, onDismiss = { deleting = null })
    }
}
