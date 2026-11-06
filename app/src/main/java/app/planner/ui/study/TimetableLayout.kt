package app.planner.ui.study

import app.planner.data.local.entity.StudyEventEntity

/** An event placed inside one day column: `column` of `columns` equal-width lanes. */
data class PlacedEvent(val event: StudyEventEntity, val column: Int, val columns: Int)

/**
 * Lays out one day's events. Events that overlap in time are put side by side in lanes;
 * events that do not overlap keep the full column width.
 */
fun layoutDay(events: List<StudyEventEntity>): List<PlacedEvent> {
    val sorted = events.sortedWith(compareBy({ it.startMinute }, { it.endMinute }))
    val result = mutableListOf<PlacedEvent>()
    var cluster = mutableListOf<Pair<StudyEventEntity, Int>>()
    val laneEnds = mutableListOf<Int>()
    var clusterEnd = -1

    fun flush() {
        val lanes = laneEnds.size
        cluster.forEach { (e, lane) -> result += PlacedEvent(e, lane, lanes) }
        cluster = mutableListOf()
        laneEnds.clear()
        clusterEnd = -1
    }

    for (e in sorted) {
        if (cluster.isNotEmpty() && e.startMinute >= clusterEnd) flush()
        var lane = laneEnds.indexOfFirst { it <= e.startMinute }
        if (lane == -1) {
            laneEnds.add(e.endMinute)
            lane = laneEnds.lastIndex
        } else {
            laneEnds[lane] = e.endMinute
        }
        cluster.add(e to lane)
        clusterEnd = maxOf(clusterEnd, e.endMinute)
    }
    flush()
    return result
}

/** First and last hour shown on the timetable: at least 08:00-18:00, widened to fit every event. */
fun gridHourRange(events: List<StudyEventEntity>): Pair<Int, Int> {
    if (events.isEmpty()) return 8 to 18
    val first = minOf(8, events.minOf { it.startMinute } / 60)
    val last = maxOf(18, (events.maxOf { it.endMinute } + 59) / 60)
    return first to last
}
