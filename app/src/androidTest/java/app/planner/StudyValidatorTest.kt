package app.planner

import app.planner.data.local.entity.*
import app.planner.domain.model.*
import app.planner.domain.validation.StudyValidator
import app.planner.domain.validation.ValidationError.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StudyValidatorTest {
    private fun event(course: Long? = 1, days: Set<Int> = setOf(1), start: Int = 540, end: Int = 630) =
        StudyEventDraft(courseId = course, type = StudyEventType.LECTURE, attendance = AttendanceFormat.ONLINE,
            days = days, startMinute = start, endMinute = end)

    @Test fun courseNeedsAName() {
        assertEquals(NAME_REQUIRED, StudyValidator.course(CourseDraft(name = "  ", colorArgb = 0)))
        assertNull(StudyValidator.course(CourseDraft(name = "Math", colorArgb = 0)))
    }

    @Test fun eventNeedsAtLeastOneValidDay() {
        assertNull(StudyValidator.studyEvent(event(days = setOf(1, 3, 5))))
        assertEquals(DAY_INVALID, StudyValidator.studyEvent(event(days = emptySet())))
        assertEquals(DAY_INVALID, StudyValidator.studyEvent(event(days = setOf(2, 8))))
    }

    @Test fun eventTimeRules() {
        assertEquals(COURSE_REQUIRED, StudyValidator.studyEvent(event(course = null)))
        assertEquals(TIME_INVALID, StudyValidator.studyEvent(event(end = 1440)))
        assertEquals(END_BEFORE_START, StudyValidator.studyEvent(event(start = 600, end = 600)))
    }

    @Test fun examTitleIsOptional() {
        fun exam(title: String = "", duration: Int = 120) =
            ExamDraft(courseId = 1, kind = ExamKind.FINAL, title = title, dateEpochDay = 20000, startMinute = 600, durationMinutes = duration)
        assertNull(StudyValidator.exam(exam()))
        assertNull(StudyValidator.exam(exam(title = "Practical")))
        assertEquals(DURATION_INVALID, StudyValidator.exam(exam(duration = 0)))
    }

    @Test fun assignmentTimeIsOptionalButValidated() {
        fun a(minute: Int?) = AssignmentDraft(courseId = 1, title = "HW", dueEpochDay = 20000, dueMinute = minute)
        assertNull(StudyValidator.assignment(a(null)))
        assertEquals(TIME_INVALID, StudyValidator.assignment(a(1440)))
    }

    @Test fun sessionLengthMustBeBetweenOneSecondAndTwentyFourHours() {
        fun s(sec: Int) = SessionDraft(durationSeconds = sec)
        assertEquals(DURATION_INVALID, StudyValidator.session(s(0)))
        assertNull(StudyValidator.session(s(1)))
        assertNull(StudyValidator.session(s(86_400)))
        assertEquals(DURATION_INVALID, StudyValidator.session(s(86_401)))
    }
}
