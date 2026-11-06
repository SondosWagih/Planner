package app.planner.ui.study

import androidx.annotation.StringRes
import app.planner.R
import app.planner.data.local.entity.AttendanceFormat
import app.planner.data.local.entity.ExamKind
import app.planner.data.local.entity.Priority
import app.planner.data.local.entity.StudyEventType
import app.planner.domain.validation.ValidationError
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

@StringRes fun StudyEventType.label() = when (this) {
    StudyEventType.LECTURE -> R.string.type_lecture
    StudyEventType.SECTION -> R.string.type_section
}

@StringRes fun AttendanceFormat.label() = when (this) {
    AttendanceFormat.PHYSICAL -> R.string.attendance_physical
    AttendanceFormat.ONLINE -> R.string.attendance_online
}

@StringRes fun ExamKind.label() = when (this) {
    ExamKind.MIDTERM -> R.string.exam_midterm
    ExamKind.FINAL -> R.string.exam_final
}

@StringRes fun Priority.label() = when (this) {
    Priority.LOW -> R.string.priority_low
    Priority.MEDIUM -> R.string.priority_medium
    Priority.HIGH -> R.string.priority_high
}

@StringRes fun ValidationError.message() = when (this) {
    ValidationError.NAME_REQUIRED -> R.string.error_name_required
    ValidationError.TITLE_REQUIRED -> R.string.error_title_required
    ValidationError.COURSE_REQUIRED -> R.string.error_course_required
    ValidationError.DAY_INVALID -> R.string.error_day_invalid
    ValidationError.TIME_INVALID -> R.string.error_time_invalid
    ValidationError.END_BEFORE_START -> R.string.error_end_before_start
    ValidationError.DURATION_INVALID -> R.string.error_duration_invalid
    ValidationError.NOT_FOUND -> R.string.error_not_found
}

fun formatMinute(minute: Int): String = String.format(Locale.getDefault(), "%02d:%02d", minute / 60, minute % 60)

fun formatDate(epochDay: Long): String =
    LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

fun dayName(dayOfWeek: Int): String = DayOfWeek.of(dayOfWeek).getDisplayName(TextStyle.FULL, Locale.getDefault())

fun shortDayName(dayOfWeek: Int): String = DayOfWeek.of(dayOfWeek).getDisplayName(TextStyle.SHORT, Locale.getDefault())

fun todayEpochDay(): Long = LocalDate.now().toEpochDay()
