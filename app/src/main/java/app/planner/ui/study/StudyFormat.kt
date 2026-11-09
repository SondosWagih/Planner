package app.planner.ui.study

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.planner.R
import app.planner.data.local.entity.AttendanceFormat
import app.planner.data.local.entity.ExamEntity
import app.planner.data.local.entity.ExamKind
import app.planner.data.local.entity.Priority
import app.planner.data.local.entity.StudyEventType
import app.planner.data.local.entity.StudySessionEntity
import app.planner.domain.validation.ValidationError
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
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

/** 12-hour time without leading zeros: "8 AM", "5 PM", "8:30 AM" in English and "8 ص", "5 م", "8:30 ص" in Arabic. */
fun formatMinute(minute: Int): String {
    val total = ((minute % 1440) + 1440) % 1440
    val h24 = total / 60
    val m = total % 60
    val arabic = Locale.getDefault().language == "ar"
    val suffix = if (h24 < 12) (if (arabic) "ص" else "AM") else (if (arabic) "م" else "PM")
    val h12 = if (h24 % 12 == 0) 12 else h24 % 12
    return if (m == 0) "$h12 $suffix" else "$h12:${m.toString().padStart(2, '0')} $suffix"
}

/** Clock-style countdown text, Latin digits: "05:09" or "1:05:09". */
fun formatClock(totalSeconds: Long): String {
    val s = totalSeconds.coerceAtLeast(0)
    val h = s / 3600
    val m = s % 3600 / 60
    val sec = s % 60
    return if (h > 0) String.format(Locale.ROOT, "%d:%02d:%02d", h, m, sec)
    else String.format(Locale.ROOT, "%02d:%02d", m, sec)
}

fun formatDate(epochDay: Long): String =
    LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

fun formatWeekRange(weekStartEpochDay: Long): String {
    val f = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
    return "${LocalDate.ofEpochDay(weekStartEpochDay).format(f)} – ${LocalDate.ofEpochDay(weekStartEpochDay + 6).format(f)}"
}

fun dayName(dayOfWeek: Int): String = DayOfWeek.of(dayOfWeek).getDisplayName(TextStyle.FULL, Locale.getDefault())

fun shortDayName(dayOfWeek: Int): String = DayOfWeek.of(dayOfWeek).getDisplayName(TextStyle.SHORT, Locale.getDefault())

fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

/** The week runs from Saturday to Friday (ISO weekdays: Saturday = 6 ... Friday = 5). */
val FIRST_DAY_OF_WEEK: DayOfWeek = DayOfWeek.SATURDAY

fun weekDaysInOrder(): List<Int> = (0..6).map { FIRST_DAY_OF_WEEK.plus(it.toLong()).value }

/** Epoch day of the Saturday that starts the current week. */
fun currentWeekStart(): Long =
    LocalDate.now().with(TemporalAdjusters.previousOrSame(FIRST_DAY_OF_WEEK)).toEpochDay()

/** A new class starts at 8 AM and ends at 9:30 AM unless changed. */
const val DEFAULT_CLASS_START_MINUTE = 8 * 60
const val DEFAULT_CLASS_END_MINUTE = 8 * 60 + 90

@Composable
fun durationText(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = totalSeconds % 3600 / 60
    val s = totalSeconds % 60
    val parts = mutableListOf<String>()
    if (h > 0) parts.add(stringResource(R.string.duration_h, h))
    if (m > 0) parts.add(stringResource(R.string.duration_m, m))
    if (s > 0 || parts.isEmpty()) parts.add(stringResource(R.string.duration_s, s))
    return parts.joinToString(" ")
}

/** An exam without a custom title is shown as "<exam type> <course name>", e.g. "Midterm Math". */
@Composable
fun examDisplayTitle(exam: ExamEntity, courseName: String?): String =
    exam.title.ifBlank { "${stringResource(exam.kind.label())} ${courseName ?: ""}".trim() }

@Composable
fun sessionDisplayTitle(session: StudySessionEntity, courseName: String?): String =
    session.title.ifBlank { courseName ?: stringResource(R.string.session_default_title) }
