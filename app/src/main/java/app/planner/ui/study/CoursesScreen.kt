package app.planner.ui.study

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.planner.R
import app.planner.data.local.entity.CourseEntity

@Composable
fun CoursesScreen(viewModel: StudyViewModel, modifier: Modifier = Modifier) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<CourseEntity?>(null) }
    var deleting by remember { mutableStateOf<CourseEntity?>(null) }

    ListScreen("screen_courses", onAdd = { creating = true }, modifier = modifier) {
        if (ui.courses.isEmpty()) item { EmptyText() }
        items(ui.courses, key = { "course_${it.id}" }) { c ->
            ItemCard(
                tint = Color(c.colorArgb),
                title = c.name,
                lines = listOfNotNull(c.code, c.instructor),
                onClick = { editing = c },
                onDelete = { deleting = c },
            )
        }
    }

    if (creating) CourseForm(null, { creating = false }, viewModel::saveCourse)
    editing?.let { c -> CourseForm(c, { editing = null }, viewModel::saveCourse) }
    deleting?.let { c ->
        DeleteDialog(R.string.delete_course_msg, onConfirm = { viewModel.deleteCourse(c.id) }, onDismiss = { deleting = null })
    }
}
