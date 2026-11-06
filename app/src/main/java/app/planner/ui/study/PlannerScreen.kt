package app.planner.ui.study

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.planner.R
import app.planner.data.local.entity.StudySessionEntity

@Composable
fun PlannerScreen(viewModel: StudyViewModel, modifier: Modifier = Modifier) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<StudySessionEntity?>(null) }
    var deleting by remember { mutableStateOf<StudySessionEntity?>(null) }
    val courseById = remember(ui.courses) { ui.courses.associateBy { it.id } }

    ListScreen("screen_planner", onAdd = { creating = true }, modifier = modifier) {
        if (ui.sessions.isEmpty()) item { EmptyText() }
        items(ui.sessions, key = { "session_${it.id}" }) { s ->
            val course = courseById[s.courseId]
            ItemCard(
                tint = course?.let { Color(it.colorArgb) } ?: Color.Gray,
                title = s.title,
                lines = listOfNotNull(
                    course?.name,
                    "${formatDate(s.dateEpochDay)} • ${formatMinute(s.startMinute)} (${stringResource(R.string.duration_min, s.durationMinutes)})",
                ),
                onClick = { editing = s },
                onDelete = { deleting = s },
                leading = { Checkbox(checked = s.completed, onCheckedChange = { viewModel.setSessionCompleted(s.id, it) }) },
            )
        }
    }

    if (creating) SessionForm(null, ui.courses, { creating = false }, viewModel::saveSession)
    editing?.let { s -> SessionForm(s, ui.courses, { editing = null }, viewModel::saveSession) }
    deleting?.let { s ->
        DeleteDialog(R.string.delete_item_msg, onConfirm = { viewModel.deleteSession(s.id) }, onDismiss = { deleting = null })
    }
}
