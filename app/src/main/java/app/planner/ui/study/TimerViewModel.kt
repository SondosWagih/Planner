package app.planner.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.planner.data.local.entity.StudySessionEntity
import app.planner.data.local.entity.totalSeconds
import app.planner.data.repository.StudyRepository
import app.planner.data.timer.TimerSnapshot
import app.planner.data.timer.TimerStatus
import app.planner.data.timer.TimerStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** remainingSeconds is rounded up, so the UI state only changes once per second. */
data class TimerUiState(
    val snapshot: TimerSnapshot = TimerSnapshot(),
    val remainingSeconds: Long = 0,
    val expired: Boolean = false,
) {
    val active: Boolean get() = snapshot.status != TimerStatus.IDLE
    val running: Boolean get() = snapshot.status == TimerStatus.RUNNING
    val totalSeconds: Long get() = snapshot.totalMs / 1000
}

class TimerViewModel(private val store: TimerStore, private val repo: StudyRepository) : ViewModel() {
    private val ticker = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(250)
        }
    }

    val state: StateFlow<TimerUiState> = combine(store.snapshot, ticker) { snap, now ->
        val raw = snap.remaining(now)
        TimerUiState(snap, (raw + 999) / 1000, expired = snap.status == TimerStatus.RUNNING && raw <= 0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TimerUiState())

    private val _finishedSessionId = MutableStateFlow<Long?>(null)
    /** Set when a timer reaches zero; the UI shows "session complete" until dismissed. */
    val finishedSessionId: StateFlow<Long?> = _finishedSessionId

    private var finishing = false

    init {
        // A timer that ended while the app was closed is detected here on the next launch.
        viewModelScope.launch {
            state.collect { s -> if (s.expired) finish(s.snapshot.sessionId) }
        }
    }

    private fun finish(sessionId: Long?) {
        if (finishing || sessionId == null) return
        finishing = true
        viewModelScope.launch {
            try {
                repo.setSessionCompleted(sessionId, true)
                store.clear()
                _finishedSessionId.value = sessionId
            } finally {
                finishing = false
            }
        }
    }

    fun start(session: StudySessionEntity) {
        if (state.value.active) return
        viewModelScope.launch { store.start(session.id, session.totalSeconds * 1000L) }
    }

    fun pause() { viewModelScope.launch { store.pause() } }
    fun resume() { viewModelScope.launch { store.resume() } }
    fun stop() { viewModelScope.launch { store.clear() } }
    fun dismissFinished() { _finishedSessionId.value = null }

    companion object {
        fun factory(store: TimerStore, repo: StudyRepository) = viewModelFactory { initializer { TimerViewModel(store, repo) } }
    }
}
