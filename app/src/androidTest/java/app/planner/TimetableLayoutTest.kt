package app.planner

import app.planner.data.local.entity.AttendanceFormat
import app.planner.data.local.entity.StudyEventEntity
import app.planner.data.local.entity.StudyEventType
import app.planner.ui.study.gridHourRange
import app.planner.ui.study.layoutDay
import org.junit.Assert.*
import org.junit.Test

class TimetableLayoutTest {
    private fun e(id: Long, start: Int, end: Int) = StudyEventEntity(
        id = id, courseId = 1, type = StudyEventType.LECTURE, attendance = AttendanceFormat.PHYSICAL,
        dayOfWeek = 1, startMinute = start, endMinute = end, createdAt = 0, updatedAt = 0,
    )

    @Test fun backToBackEventsKeepFullWidth() {
        val placed = layoutDay(listOf(e(1, 540, 600), e(2, 600, 660)))
        assertTrue(placed.all { it.columns == 1 && it.column == 0 })
    }

    @Test fun overlappingEventsSitSideBySide() {
        val placed = layoutDay(listOf(e(1, 540, 660), e(2, 600, 720)))
        assertTrue(placed.all { it.columns == 2 })
        assertEquals(setOf(0, 1), placed.map { it.column }.toSet())
    }

    @Test fun laneIsReusedOnceAnEventEnds() {
        val placed = layoutDay(listOf(e(1, 540, 600), e(2, 570, 660), e(3, 600, 630))).associateBy { it.event.id }
        assertEquals(0, placed.getValue(1).column)
        assertEquals(1, placed.getValue(2).column)
        assertEquals(0, placed.getValue(3).column) // lane 0 is free again at 10:00
        assertTrue(placed.values.all { it.columns == 2 })
    }

    @Test fun separateClustersAreIndependent() {
        val placed = layoutDay(listOf(e(1, 540, 660), e(2, 600, 700), e(3, 900, 960))).associateBy { it.event.id }
        assertEquals(2, placed.getValue(1).columns)
        assertEquals(1, placed.getValue(3).columns)
    }

    @Test fun gridShowsAtLeastEightToSixAndWidensToFit() {
        assertEquals(8 to 18, gridHourRange(emptyList()))
        assertEquals(8 to 18, gridHourRange(listOf(e(1, 600, 660))))
        assertEquals(6 to 22, gridHourRange(listOf(e(1, 360, 420), e(2, 1200, 1290))))
    }
}
