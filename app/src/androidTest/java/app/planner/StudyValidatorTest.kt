package app.planner

import app.planner.data.local.entity.*
import app.planner.domain.model.*
import app.planner.domain.validation.StudyValidator
import app.planner.domain.validation.ValidationError.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StudyValidatorTest {
    private fun event(course: Long? = 1, day: Int = 1, start: Int = 540, end: Int = 630) =
        StudyEventDraft(courseId = course, type = StudyEventType.LECTURE, attendance = AttendanceFormat.ONLINE,
            dayOfWeek = day, startMinute = start, endMinute = end)

    @Test fun courseNeedsAName() {
        assertEquals(NAME_REQUIRED, StudyValidator.course(CourseDraft(name = "  ", colorArgb = 0)))
        assertNull(StudyValidator.course(CourseDraft(name = "Math", colorArgb = 0)))
    }

    @Test fun eventRules() {
        assertNull(StudyValidator.studyEvent(event()))
        assertEquals(COURSE_REQUIRED, StudyValidator.studyEvent(event(course = null)))
        assertEquals(DAY_INVALID, StudyValidator.studyEvent(event(day = 8)))
        assertEquals(TIME_INVALID, StudyValidator.studyEvent(event(end = 1440)))
        assertEquals(END_BEFORE_START, StudyValidator.studyEvent(event(start = 600, end = 600)))
    }

    @Test fun examRules() {
        fun exam(title: String = "Final", duration: Int = 120) =
            ExamDraft(courseId = 1, kind = ExamKind.FINAL, title = title, dateEpochDay = 20000, startMinute = 600, durationMinutes = duration)
        assertNull(StudyValidator.exam(exam()))
        assertEquals(TITLE_REQUIRED, StudyValidator.exam(exam(title = "")))
        assertEquals(DURATION_INVALID, StudyValidator.exam(exam(duration = 0)))
    }

    @Test fun assignmentTimeIsOptionalButValidated() {
        fun a(minute: Int?) = AssignmentDraft(courseId = 1, title = "HW", dueEpochDay = 20000, dueMinute = minute)
        assertNull(StudyValidator.assignment(a(null)))
        assertNull(StudyValidator.assignment(a(1439)))
        assertEquals(TIME_INVALID, StudyValidator.assignment(a(1440)))
    }

    @Test fun sessionCourseIsOptional() {
        val s = SessionDraft(courseId = null, title = "Review", dateEpochDay = 20000, startMinute = 1000, durationMinutes = 45)
        assertNull(StudyValidator.session(s))
    }
}
