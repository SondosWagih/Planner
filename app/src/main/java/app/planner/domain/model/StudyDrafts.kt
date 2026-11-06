package app.planner.domain.model

import app.planner.data.local.entity.AttendanceFormat
import app.planner.data.local.entity.ExamKind
import app.planner.data.local.entity.Priority
import app.planner.data.local.entity.StudyEventType

/** Form input models. id == null means "create", otherwise "edit that row". */
data class CourseDraft(
    val id: Long? = null, val name: String, val code: String? = null,
    val instructor: String? = null, val colorArgb: Int,
)

data class StudyEventDraft(
    val id: Long? = null, val courseId: Long?, val type: StudyEventType, val attendance: AttendanceFormat,
    val dayOfWeek: Int, val startMinute: Int, val endMinute: Int,
    val location: String? = null, val onlineLink: String? = null, val notes: String? = null,
)

data class ExamDraft(
    val id: Long? = null, val courseId: Long?, val kind: ExamKind, val title: String,
    val dateEpochDay: Long, val startMinute: Int, val durationMinutes: Int,
    val location: String? = null, val notes: String? = null,
)

data class AssignmentDraft(
    val id: Long? = null, val courseId: Long?, val title: String, val description: String? = null,
    val dueEpochDay: Long, val dueMinute: Int? = null, val priority: Priority = Priority.MEDIUM,
)

data class SessionDraft(
    val id: Long? = null, val courseId: Long?, val title: String, val dateEpochDay: Long,
    val startMinute: Int, val durationMinutes: Int, val notes: String? = null,
)
