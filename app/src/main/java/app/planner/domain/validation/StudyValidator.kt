package app.planner.domain.validation

import app.planner.domain.model.*

enum class ValidationError {
    NAME_REQUIRED, TITLE_REQUIRED, COURSE_REQUIRED, DAY_INVALID,
    TIME_INVALID, END_BEFORE_START, DURATION_INVALID, NOT_FOUND,
}

/** Pure functions: return null when valid, otherwise the first problem found. */
object StudyValidator {
    private fun minuteOk(m: Int) = m in 0..1439
    private fun durationOk(d: Int) = d in 1..1440

    fun course(d: CourseDraft): ValidationError? =
        if (d.name.isBlank()) ValidationError.NAME_REQUIRED else null

    fun studyEvent(d: StudyEventDraft): ValidationError? = when {
        d.courseId == null -> ValidationError.COURSE_REQUIRED
        d.dayOfWeek !in 1..7 -> ValidationError.DAY_INVALID
        !minuteOk(d.startMinute) || !minuteOk(d.endMinute) -> ValidationError.TIME_INVALID
        d.endMinute <= d.startMinute -> ValidationError.END_BEFORE_START
        else -> null
    }

    fun exam(d: ExamDraft): ValidationError? = when {
        d.courseId == null -> ValidationError.COURSE_REQUIRED
        d.title.isBlank() -> ValidationError.TITLE_REQUIRED
        !minuteOk(d.startMinute) -> ValidationError.TIME_INVALID
        !durationOk(d.durationMinutes) -> ValidationError.DURATION_INVALID
        else -> null
    }

    fun assignment(d: AssignmentDraft): ValidationError? = when {
        d.courseId == null -> ValidationError.COURSE_REQUIRED
        d.title.isBlank() -> ValidationError.TITLE_REQUIRED
        d.dueMinute != null && !minuteOk(d.dueMinute) -> ValidationError.TIME_INVALID
        else -> null
    }

    /** Course is optional for a study session. */
    fun session(d: SessionDraft): ValidationError? = when {
        d.title.isBlank() -> ValidationError.TITLE_REQUIRED
        !minuteOk(d.startMinute) -> ValidationError.TIME_INVALID
        !durationOk(d.durationMinutes) -> ValidationError.DURATION_INVALID
        else -> null
    }
}
