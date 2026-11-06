package app.planner.ui.study

import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import app.planner.R
import app.planner.data.local.entity.*
import app.planner.domain.model.*
import app.planner.domain.validation.ValidationError
import java.time.LocalDate

typealias SaveHandler<D> = (D, (ValidationError?) -> Unit) -> Unit

@Composable
fun CourseForm(initial: CourseEntity?, onDismiss: () -> Unit, onSave: SaveHandler<CourseDraft>) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var code by remember { mutableStateOf(initial?.code ?: "") }
    var instructor by remember { mutableStateOf(initial?.instructor ?: "") }
    var color by remember { mutableIntStateOf(initial?.colorArgb ?: DefaultCourseColor) }
    var error by remember { mutableStateOf<ValidationError?>(null) }
    FormDialog(stringResource(R.string.form_course), error, onDismiss, onSave = {
        onSave(CourseDraft(initial?.id, name, code, instructor, color)) { e -> if (e == null) onDismiss() else error = e }
    }) {
        TextInput(stringResource(R.string.field_name), name) { name = it }
        TextInput(stringResource(R.string.field_code), code) { code = it }
        TextInput(stringResource(R.string.field_instructor), instructor) { instructor = it }
        ColorPicker(stringResource(R.string.field_color), color) { color = it }
    }
}

@Composable
fun EventForm(initial: StudyEventEntity?, courses: List<CourseEntity>, onDismiss: () -> Unit, onSave: SaveHandler<StudyEventDraft>, onDelete: (() -> Unit)? = null) {
    var courseId by remember { mutableStateOf(initial?.courseId ?: courses.firstOrNull()?.id) }
    var type by remember { mutableStateOf(initial?.type ?: StudyEventType.LECTURE) }
    var attendance by remember { mutableStateOf(initial?.attendance ?: AttendanceFormat.PHYSICAL) }
    var day by remember { mutableIntStateOf(initial?.dayOfWeek ?: LocalDate.now().dayOfWeek.value) }
    var start by remember { mutableIntStateOf(initial?.startMinute ?: 540) }
    var end by remember { mutableIntStateOf(initial?.endMinute ?: 630) }
    var location by remember { mutableStateOf(initial?.location ?: "") }
    var link by remember { mutableStateOf(initial?.onlineLink ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var error by remember { mutableStateOf<ValidationError?>(null) }
    FormDialog(stringResource(R.string.form_class), error, onDismiss, onSave = {
        onSave(StudyEventDraft(initial?.id, courseId, type, attendance, day, start, end, location, link, notes)) { e ->
            if (e == null) onDismiss() else error = e
        }
    }, onDelete = onDelete) {
        CourseChoice(courses, courseId, allowNone = false) { courseId = it }
        ChoiceRow(stringResource(R.string.field_type), StudyEventType.entries.toList(), type, { type = it }) { stringResource(it.label()) }
        ChoiceRow(stringResource(R.string.field_attendance), AttendanceFormat.entries.toList(), attendance, { attendance = it }) { stringResource(it.label()) }
        ChoiceRow(stringResource(R.string.field_day), (1..7).toList(), day, { day = it }) { dayName(it) }
        TimeField(stringResource(R.string.field_start), start, { start = it })
        TimeField(stringResource(R.string.field_end), end, { end = it })
        TextInput(stringResource(R.string.field_location), location) { location = it }
        TextInput(stringResource(R.string.field_link), link) { link = it }
        TextInput(stringResource(R.string.field_notes), notes, singleLine = false) { notes = it }
    }
}

@Composable
fun ExamForm(initial: ExamEntity?, courses: List<CourseEntity>, onDismiss: () -> Unit, onSave: SaveHandler<ExamDraft>) {
    var courseId by remember { mutableStateOf(initial?.courseId ?: courses.firstOrNull()?.id) }
    var kind by remember { mutableStateOf(initial?.kind ?: ExamKind.MIDTERM) }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var date by remember { mutableLongStateOf(initial?.dateEpochDay ?: todayEpochDay()) }
    var start by remember { mutableIntStateOf(initial?.startMinute ?: 540) }
    var duration by remember { mutableStateOf((initial?.durationMinutes ?: 120).toString()) }
    var location by remember { mutableStateOf(initial?.location ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var error by remember { mutableStateOf<ValidationError?>(null) }
    FormDialog(stringResource(R.string.form_exam), error, onDismiss, onSave = {
        onSave(ExamDraft(initial?.id, courseId, kind, title, date, start, duration.trim().toIntOrNull() ?: 0, location, notes)) { e ->
            if (e == null) onDismiss() else error = e
        }
    }) {
        CourseChoice(courses, courseId, allowNone = false) { courseId = it }
        ChoiceRow(stringResource(R.string.field_exam_kind), ExamKind.entries.toList(), kind, { kind = it }) { stringResource(it.label()) }
        TextInput(stringResource(R.string.field_title), title) { title = it }
        DateField(stringResource(R.string.field_date), date) { date = it }
        TimeField(stringResource(R.string.field_start), start, { start = it })
        TextInput(stringResource(R.string.field_duration), duration, numeric = true) { duration = it }
        TextInput(stringResource(R.string.field_location), location) { location = it }
        TextInput(stringResource(R.string.field_notes), notes, singleLine = false) { notes = it }
    }
}

@Composable
fun AssignmentForm(initial: AssignmentEntity?, courses: List<CourseEntity>, onDismiss: () -> Unit, onSave: SaveHandler<AssignmentDraft>) {
    var courseId by remember { mutableStateOf(initial?.courseId ?: courses.firstOrNull()?.id) }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var due by remember { mutableLongStateOf(initial?.dueEpochDay ?: todayEpochDay()) }
    var dueMinute by remember { mutableStateOf(initial?.dueMinute) }
    var priority by remember { mutableStateOf(initial?.priority ?: Priority.MEDIUM) }
    var error by remember { mutableStateOf<ValidationError?>(null) }
    FormDialog(stringResource(R.string.form_assignment), error, onDismiss, onSave = {
        onSave(AssignmentDraft(initial?.id, courseId, title, description, due, dueMinute, priority)) { e ->
            if (e == null) onDismiss() else error = e
        }
    }) {
        CourseChoice(courses, courseId, allowNone = false) { courseId = it }
        TextInput(stringResource(R.string.field_title), title) { title = it }
        TextInput(stringResource(R.string.field_description), description, singleLine = false) { description = it }
        DateField(stringResource(R.string.field_date), due) { due = it }
        TimeField(stringResource(R.string.field_time), dueMinute, { dueMinute = it }, onClear = { dueMinute = null })
        ChoiceRow(stringResource(R.string.field_priority), Priority.entries.toList(), priority, { priority = it }) { stringResource(it.label()) }
    }
}

@Composable
fun SessionForm(initial: StudySessionEntity?, courses: List<CourseEntity>, onDismiss: () -> Unit, onSave: SaveHandler<SessionDraft>) {
    var courseId by remember { mutableStateOf(initial?.courseId) }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var date by remember { mutableLongStateOf(initial?.dateEpochDay ?: todayEpochDay()) }
    var start by remember { mutableIntStateOf(initial?.startMinute ?: 1020) }
    var duration by remember { mutableStateOf((initial?.durationMinutes ?: 60).toString()) }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var error by remember { mutableStateOf<ValidationError?>(null) }
    FormDialog(stringResource(R.string.form_session), error, onDismiss, onSave = {
        onSave(SessionDraft(initial?.id, courseId, title, date, start, duration.trim().toIntOrNull() ?: 0, notes)) { e ->
            if (e == null) onDismiss() else error = e
        }
    }) {
        CourseChoice(courses, courseId, allowNone = true) { courseId = it }
        TextInput(stringResource(R.string.field_title), title) { title = it }
        DateField(stringResource(R.string.field_date), date) { date = it }
        TimeField(stringResource(R.string.field_start), start, { start = it })
        TextInput(stringResource(R.string.field_duration), duration, numeric = true) { duration = it }
        TextInput(stringResource(R.string.field_notes), notes, singleLine = false) { notes = it }
    }
}
