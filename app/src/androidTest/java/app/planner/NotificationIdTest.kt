package app.planner

import app.planner.data.local.entity.NotificationKind
import app.planner.data.local.entity.NotificationOwnerType
import app.planner.domain.schedule.AlarmPlanner
import app.planner.notifications.Notifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationIdTest {
    private val fixedIds = setOf(Notifier.ID_TIMER, Notifier.ID_TIMER_DONE, Notifier.ID_OVERDUE, Notifier.ID_TEST)

    @Test fun anExamOrClassAlertNeverUsesTheIdOfTheTimerOrOverdueNotification() {
        for (type in listOf(NotificationOwnerType.EXAM, NotificationOwnerType.STUDY_EVENT)) {
            for (kind in NotificationKind.entries) {
                for (id in 0L..50L) {
                    val itemId = Notifier.itemNotificationId(AlarmPlanner.requestCode(type, id, kind))
                    assertFalse("$type/$kind/$id collides with a fixed notification id", itemId in fixedIds)
                    assertTrue(itemId > 0)
                }
            }
        }
    }

    @Test fun differentAlertsKeepDifferentIds() {
        val a = Notifier.itemNotificationId(AlarmPlanner.requestCode(NotificationOwnerType.EXAM, 3, NotificationKind.ADVANCE_48H))
        val b = Notifier.itemNotificationId(AlarmPlanner.requestCode(NotificationOwnerType.EXAM, 3, NotificationKind.AT_TIME))
        val c = Notifier.itemNotificationId(AlarmPlanner.requestCode(NotificationOwnerType.STUDY_EVENT, 3, NotificationKind.AT_TIME))
        assertEquals(3, setOf(a, b, c).size)
    }
}
