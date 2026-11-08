package app.planner.ui.study

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.planner.R
import app.planner.data.local.entity.AttendanceFormat
import app.planner.data.local.entity.StudyEventEntity
import java.time.LocalDate

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
        val todayDow = LocalDate.now().dayOfWeek.value
        val days = weekDaysInOrder().map { TimetableDay(key = it, label = shortDayName(it), highlight = it == todayDow) }
        val items = ui.events.map { e ->
            val course = courseById[e.courseId]
            TimetableItem(
                id = e.id,
                dayKey = e.dayOfWeek,
                startMinute = e.startMinute,
                endMinute = e.endMinute,
                title = course?.name ?: "—",
                lines = listOfNotNull(
                    "${formatMinute(e.startMinute)} – ${formatMinute(e.endMinute)}",
                    "${stringResource(e.type.label())} • ${stringResource(e.attendance.label())}",
                    e.location,
                ),
                color = course?.let { Color(it.colorArgb) } ?: Color.Gray,
                icon = if (e.attendance == AttendanceFormat.ONLINE) Icons.Outlined.Videocam else Icons.Outlined.Place,
            )
        }
        Box(modifier.fillMaxSize().testTag("screen_schedule")) {
            Timetable(
                days = days,
                items = items,
                onItemClick = { id -> ui.events.firstOrNull { it.id == id }?.let { dialog = ScheduleDialog.Edit(it) } },
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
