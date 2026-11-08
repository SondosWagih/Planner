package app.planner.domain.validation

import app.planner.domain.model.*

enum class ValidationError {
    NAME_REQUIRED, TITLE_REQUIRED, COURSE_REQUIRED, DAY_INVALID,
    TIME_INVALID, END_BEFORE_START, DURATION_INVALID, NOT_FOUND,
}

/** Pure functions: return null when valid, otherwise the first problem found. */
object StudyValidator {
    const val MAX_SESSION_SECONDS = 24 * 60 * 60

    private fun minuteOk(m: Int) = m in 0..1439

    fun course(d: CourseDraft): ValidationError? =
        if (d.name.isBlank()) ValidationError.NAME_REQUIRED else null

    /** At least one weekday must be chosen. */
    fun studyEvent(d: StudyEventDraft): ValidationError? = when {
        d.courseId == null -> ValidationError.COURSE_REQUIRED
        d.days.isEmpty() || d.days.any { it !in 1..7 } -> ValidationError.DAY_INVALID
        !minuteOk(d.startMinute) || !minuteOk(d.endMinute) -> ValidationError.TIME_INVALID
        d.endMinute <= d.startMinute -> ValidationError.END_BEFORE_START
        else -> null
    }

    /** The title is optional (it defaults to "<exam type> <course>"). */
    fun exam(d: ExamDraft): ValidationError? = when {
        d.courseId == null -> ValidationError.COURSE_REQUIRED
        !minuteOk(d.startMinute) -> ValidationError.TIME_INVALID
        d.durationMinutes !in 1..1440 -> ValidationError.DURATION_INVALID
        else -> null
    }

    fun assignment(d: AssignmentDraft): ValidationError? = when {
        d.courseId == null -> ValidationError.COURSE_REQUIRED
        d.title.isBlank() -> ValidationError.TITLE_REQUIRED
        d.dueMinute != null && !minuteOk(d.dueMinute) -> ValidationError.TIME_INVALID
        else -> null
    }

    /** Course and title are optional; the length must be 1 second .. 24 hours. */
    fun session(d: SessionDraft): ValidationError? =
        if (d.durationSeconds !in 1..MAX_SESSION_SECONDS) ValidationError.DURATION_INVALID else null
}
