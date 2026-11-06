package app.planner

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.planner.data.local.db.AppDatabase
import app.planner.data.local.entity.*
import app.planner.data.repository.StudyRepository
import app.planner.domain.model.*
import app.planner.domain.validation.ValidationError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudyRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repo: StudyRepository

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).build()
        repo = StudyRepository(db) { 1_700_000_000_000L }
    }
    @After fun tearDown() = db.close()

    private suspend fun newCourse(name: String = "Math"): Long {
        assertNull(repo.saveCourse(CourseDraft(name = name, colorArgb = 0xFF3F6FD8.toInt())))
        return repo.courses.first().first { it.name == name }.id
    }

    private fun event(courseId: Long, type: StudyEventType, attendance: AttendanceFormat, start: Int = 540, end: Int = 630) =
        StudyEventDraft(courseId = courseId, type = type, attendance = attendance, dayOfWeek = 2, startMinute = start, endMinute = end)

    @Test fun invalidEventIsRejectedAndNothingIsStored() = runBlocking {
        val cid = newCourse()
        val err = repo.saveEvent(event(cid, StudyEventType.LECTURE, AttendanceFormat.PHYSICAL, start = 600, end = 540))
        assertEquals(ValidationError.END_BEFORE_START, err)
        assertTrue(repo.events.first().isEmpty())
    }

    @Test fun lectureAndSectionAcceptBothAttendanceFormats() = runBlocking {
        val cid = newCourse()
        for (t in StudyEventType.entries) for (a in AttendanceFormat.entries) assertNull(repo.saveEvent(event(cid, t, a)))
        assertEquals(4, repo.events.first().size)
    }

    @Test fun editingUpdatesTheSameRowInsteadOfDuplicating() = runBlocking {
        val cid = newCourse()
        repo.saveEvent(event(cid, StudyEventType.LECTURE, AttendanceFormat.PHYSICAL))
        val saved = repo.events.first().single()
        assertNull(repo.saveEvent(event(cid, StudyEventType.SECTION, AttendanceFormat.ONLINE).copy(id = saved.id)))
        val after = repo.events.first().single()
        assertEquals(saved.id, after.id)
        assertEquals(StudyEventType.SECTION, after.type)
        assertEquals(AttendanceFormat.ONLINE, after.attendance)
    }

    @Test fun eventForMissingCourseIsRejected() = runBlocking {
        val err = repo.saveEvent(event(999, StudyEventType.LECTURE, AttendanceFormat.ONLINE))
        assertEquals(ValidationError.COURSE_REQUIRED, err)
    }

    @Test fun deletingACourseHidesAllItsChildren() = runBlocking {
        val cid = newCourse()
        repo.saveEvent(event(cid, StudyEventType.LECTURE, AttendanceFormat.PHYSICAL))
        repo.saveExam(ExamDraft(courseId = cid, kind = ExamKind.MIDTERM, title = "Mid", dateEpochDay = 20000, startMinute = 600, durationMinutes = 90))
        repo.saveAssignment(AssignmentDraft(courseId = cid, title = "HW1", dueEpochDay = 20001))
        repo.saveSession(SessionDraft(courseId = cid, title = "Review", dateEpochDay = 20002, startMinute = 1000, durationMinutes = 60))
        repo.deleteCourse(cid)
        assertTrue(repo.courses.first().isEmpty())
        assertTrue(repo.events.first().isEmpty())
        assertTrue(repo.exams.first().isEmpty())
        assertTrue(repo.assignments.first().isEmpty())
        assertTrue(repo.sessions.first().isEmpty())
    }

    @Test fun assignmentCompletionCanBeToggled() = runBlocking {
        val cid = newCourse()
        repo.saveAssignment(AssignmentDraft(courseId = cid, title = "HW1", dueEpochDay = 20001))
        val id = repo.assignments.first().single().id
        repo.setAssignmentCompleted(id, true)
        assertTrue(repo.assignments.first().single().completed)
        assertNotNull(repo.assignments.first().single().completedAt)
        repo.setAssignmentCompleted(id, false)
        assertFalse(repo.assignments.first().single().completed)
        assertNull(repo.assignments.first().single().completedAt)
    }
}
