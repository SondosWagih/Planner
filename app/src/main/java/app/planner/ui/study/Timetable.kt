package app.planner.ui.study

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.planner.data.local.entity.AttendanceFormat
import app.planner.data.local.entity.CourseEntity
import app.planner.data.local.entity.StudyEventEntity
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

private val TimeColumnWidth = 52.dp
private val HourHeight = 72.dp
private val HeaderHeight = 40.dp
private val MinDayWidth = 112.dp

/**
 * Real weekly timetable: hours down the side, days across the top, classes as colour cards.
 * The header and the hour column stay put while the grid scrolls (vertically and, on narrow
 * screens, horizontally). Layout mirrors automatically in RTL.
 */
@Composable
fun WeeklyTimetable(
    events: List<StudyEventEntity>,
    courses: Map<Long, CourseEntity>,
    onEventClick: (StudyEventEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val (startHour, endHour) = remember(events) { gridHourRange(events) }
    val hours = endHour - startHour
    val firstDay = WeekFields.of(Locale.getDefault()).firstDayOfWeek.value
    val days = remember(firstDay) { (0..6).map { (firstDay - 1 + it) % 7 + 1 } }
    val today = LocalDate.now().dayOfWeek.value
    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()
    val lineColor = MaterialTheme.colorScheme.outlineVariant

    BoxWithConstraints(modifier) {
        val dayWidth = maxOf((maxWidth - TimeColumnWidth) / 7, MinDayWidth)
        Column(Modifier.fillMaxSize()) {
            // Sticky header: day names
            Row(Modifier.fillMaxWidth().height(HeaderHeight)) {
                Spacer(Modifier.width(TimeColumnWidth))
                Row(Modifier.weight(1f).horizontalScroll(hScroll)) {
                    days.forEach { d ->
                        Box(Modifier.width(dayWidth).fillMaxHeight(), contentAlignment = Alignment.Center) {
                            Text(
                                shortDayName(d),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (d == today) FontWeight.Bold else FontWeight.Normal,
                                color = if (d == today) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
            HorizontalDivider()
            // Body: hour column (fixed horizontally) + day columns (scroll with the header)
            Row(Modifier.weight(1f).fillMaxWidth().verticalScroll(vScroll)) {
                Column(Modifier.width(TimeColumnWidth)) {
                    for (h in startHour until endHour) {
                        Box(Modifier.fillMaxWidth().height(HourHeight).padding(top = 2.dp), contentAlignment = Alignment.TopCenter) {
                            Text(
                                formatMinute(h * 60),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Row(Modifier.weight(1f).horizontalScroll(hScroll)) {
                    days.forEach { d ->
                        DayColumn(
                            events = events.filter { it.dayOfWeek == d },
                            courses = courses,
                            startHour = startHour,
                            hours = hours,
                            width = dayWidth,
                            highlight = d == today,
                            lineColor = lineColor,
                            onEventClick = onEventClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayColumn(
    events: List<StudyEventEntity>,
    courses: Map<Long, CourseEntity>,
    startHour: Int,
    hours: Int,
    width: Dp,
    highlight: Boolean,
    lineColor: Color,
    onEventClick: (StudyEventEntity) -> Unit,
) {
    val placed = remember(events) { layoutDay(events) }
    val tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
    Box(
        Modifier.width(width).height(HourHeight * hours)
            .background(if (highlight) tint else Color.Transparent)
            .drawBehind {
                val step = HourHeight.toPx()
                val stroke = 1.dp.toPx()
                for (i in 0..hours) drawLine(lineColor, Offset(0f, i * step), Offset(size.width, i * step), stroke)
                drawLine(lineColor, Offset(size.width, 0f), Offset(size.width, size.height), stroke)
            }
    ) {
        placed.forEach { p ->
            val laneWidth = width / p.columns
            val top = HourHeight * ((p.event.startMinute - startHour * 60) / 60f)
            val height = maxOf(HourHeight * ((p.event.endMinute - p.event.startMinute) / 60f), 32.dp)
            EventCard(
                event = p.event,
                course = courses[p.event.courseId],
                onClick = { onEventClick(p.event) },
                modifier = Modifier.offset(x = laneWidth * p.column, y = top).size(laneWidth, height).padding(1.5.dp),
            )
        }
    }
}

@Composable
private fun EventCard(event: StudyEventEntity, course: CourseEntity?, onClick: () -> Unit, modifier: Modifier) {
    val bg = course?.let { Color(it.colorArgb) } ?: Color.Gray
    val fg = if (bg.luminance() > 0.5f) Color.Black else Color.White
    Surface(
        onClick = onClick,
        color = bg,
        contentColor = fg,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.testTag("event_card"),
    ) {
        Column(Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
            Text(course?.name ?: "—", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (event.attendance == AttendanceFormat.ONLINE) Icons.Outlined.Videocam else Icons.Outlined.Place,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    "${stringResource(event.type.label())} • ${stringResource(event.attendance.label())}",
                    fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            Text("${formatMinute(event.startMinute)}–${formatMinute(event.endMinute)}", fontSize = 10.sp, maxLines = 1)
            event.location?.let { Text(it, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
    }
}
