package app.planner.ui.study

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.planner.R
import app.planner.data.local.entity.ExamEntity

@Composable
fun ExamsScreen(viewModel: StudyViewModel, modifier: Modifier = Modifier) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ExamEntity?>(null) }
    var deleting by remember { mutableStateOf<ExamEntity?>(null) }
    val courseById = remember(ui.courses) { ui.courses.associateBy { it.id } }

    if (ui.courses.isEmpty()) {
        CenteredMessage("screen_exams", stringResource(R.string.add_course_first))
    } else {
        ListScreen("screen_exams", onAdd = { creating = true }, modifier = modifier) {
            if (ui.exams.isEmpty()) item { EmptyText() }
            items(ui.exams, key = { "exam_${it.id}" }) { x ->
                val course = courseById[x.courseId]
                ItemCard(
                    tint = course?.let { Color(it.colorArgb) } ?: Color.Gray,
                    title = x.title,
                    lines = listOfNotNull(
                        "${course?.name ?: "—"} • ${stringResource(x.kind.label())}",
                        "${formatDate(x.dateEpochDay)} • ${formatMinute(x.startMinute)} (${stringResource(R.string.duration_min, x.durationMinutes)})",
                        x.location,
                    ),
                    onClick = { editing = x },
                    onDelete = { deleting = x },
                )
            }
        }
    }

    if (creating) ExamForm(null, ui.courses, { creating = false }, viewModel::saveExam)
    editing?.let { x -> ExamForm(x, ui.courses, { editing = null }, viewModel::saveExam) }
    deleting?.let { x ->
        DeleteDialog(R.string.delete_item_msg, onConfirm = { viewModel.deleteExam(x.id) }, onDismiss = { deleting = null })
    }
}
