package app.planner.ui.study

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.planner.R
import app.planner.data.local.entity.AssignmentEntity

@Composable
fun AssignmentsScreen(viewModel: StudyViewModel, modifier: Modifier = Modifier) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<AssignmentEntity?>(null) }
    var deleting by remember { mutableStateOf<AssignmentEntity?>(null) }
    val courseById = remember(ui.courses) { ui.courses.associateBy { it.id } }
    val today = todayEpochDay()

    if (ui.courses.isEmpty()) {
        CenteredMessage("screen_assignments", stringResource(R.string.add_course_first))
    } else {
        ListScreen("screen_assignments", onAdd = { creating = true }, modifier = modifier) {
            if (ui.assignments.isEmpty()) item { EmptyText() }
            items(ui.assignments, key = { "assignment_${it.id}" }) { a ->
                val course = courseById[a.courseId]
                val overdue = !a.completed && a.dueEpochDay < today
                ItemCard(
                    tint = course?.let { Color(it.colorArgb) } ?: Color.Gray,
                    title = a.title,
                    lines = listOfNotNull(
                        "${course?.name ?: "—"} • ${stringResource(a.priority.label())}",
                        formatDate(a.dueEpochDay) + (a.dueMinute?.let { " • ${formatMinute(it)}" } ?: ""),
                        if (overdue) stringResource(R.string.overdue) else null,
                    ),
                    onClick = { editing = a },
                    onDelete = { deleting = a },
                    leading = { Checkbox(checked = a.completed, onCheckedChange = { viewModel.setAssignmentCompleted(a.id, it) }) },
                )
            }
        }
    }

    if (creating) AssignmentForm(null, ui.courses, { creating = false }, viewModel::saveAssignment)
    editing?.let { a -> AssignmentForm(a, ui.courses, { editing = null }, viewModel::saveAssignment) }
    deleting?.let { a ->
        DeleteDialog(R.string.delete_item_msg, onConfirm = { viewModel.deleteAssignment(a.id) }, onDismiss = { deleting = null })
    }
}
