package app.planner.notifications

import app.planner.data.local.db.AppDatabase
import app.planner.data.local.entity.CourseEntity
import app.planner.data.local.entity.NotificationKind
import app.planner.data.local.entity.NotificationOwnerType
import app.planner.data.local.entity.NotificationStatus
import app.planner.domain.schedule.AlarmPlanner
import app.planner.domain.schedule.WeekMath
import java.time.ZoneId

/** What happens when an alarm fires. Always ends by planning the next alarms. */
class AlarmHandler(
    private val db: AppDatabase,
    private val notifier: Notifier,
    private val scheduler: NotificationScheduler,
    private val timer: TimerController,
    private val now: () -> Long = { System.currentTimeMillis() },
    private val zone: () -> ZoneId = { ZoneId.systemDefault() },
) {
    suspend fun handle(type: NotificationOwnerType, id: Long, kind: NotificationKind, occurrenceKey: Long) {
        val code = AlarmPlanner.requestCode(type, id, kind)
        when (type) {
            NotificationOwnerType.STUDY_EVENT -> {
                val event = db.studyEventDao().get(id)?.takeIf { it.deletedAt == null }
                if (event != null) notifier.showClassNow(event, db.courseDao().get(event.courseId), code)
            }
            NotificationOwnerType.EXAM -> {
                val exam = db.examDao().get(id)?.takeIf { it.deletedAt == null }
                if (exam != null) notifier.showExam(exam, db.courseDao().get(exam.courseId), kind, code)
            }
            NotificationOwnerType.ASSIGNMENT -> showOverdueNow()
            NotificationOwnerType.SESSION -> timer.completeIfExpired(toleranceMs = 1_500)
        }
        db.notificationRecordDao().markFired(code, NotificationStatus.FIRED, now())
        scheduler.rescheduleAll()
    }

    /** One summary of everything that is past its day and not ticked yet (nothing if there is none). */
    suspend fun showOverdueNow() {
        val today = WeekMath.epochDayOf(now(), zone())
        val overdue = db.assignmentDao().getIncomplete().filter { it.dueEpochDay < today }
        if (overdue.isEmpty()) {
            notifier.cancelOverdue()
            return
        }
        val courses = mutableMapOf<Long, CourseEntity?>()
        val items = overdue.sortedBy { it.dueEpochDay }.map { a ->
            a to courses.getOrPut(a.courseId) { db.courseDao().get(a.courseId) }
        }
        notifier.showOverdue(items)
    }
}
