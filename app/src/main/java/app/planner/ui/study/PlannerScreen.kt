package app.planner.ui.study

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.planner.R
import app.planner.data.local.entity.StudySessionEntity
import app.planner.data.local.entity.totalSeconds

/** Study sessions as timers: pick a session, press Start, the countdown runs. */
@Composable
fun PlannerScreen(viewModel: StudyViewModel, timer: TimerViewModel, modifier: Modifier = Modifier) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    val t by timer.state.collectAsStateWithLifecycle()
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<StudySessionEntity?>(null) }
    var deleting by remember { mutableStateOf<StudySessionEntity?>(null) }
    var confirmStop by remember { mutableStateOf(false) }
    val courseById = remember(ui.courses) { ui.courses.associateBy { it.id } }

    // Keep the screen awake while a countdown is running.
    val view = LocalView.current
    DisposableEffect(t.running) {
        view.keepScreenOn = t.running
        onDispose { view.keepScreenOn = false }
    }

    Box(modifier.fillMaxSize().testTag("screen_planner")) {
        Column(Modifier.fillMaxSize()) {
            if (t.active) {
                val active = ui.sessions.firstOrNull { it.id == t.snapshot.sessionId }
                TimerPanel(
                    title = active?.let { sessionDisplayTitle(it, courseById[it.courseId]?.name) } ?: "",
                    remainingSeconds = t.remainingSeconds,
                    totalSeconds = t.totalSeconds,
                    running = t.running,
                    onPause = timer::pause,
                    onResume = timer::resume,
                    onStop = { confirmStop = true },
                    modifier = Modifier.padding(16.dp),
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp, if (t.active) 0.dp else 16.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (ui.sessions.isEmpty()) item { EmptyText() }
                items(ui.sessions, key = { "session_${it.id}" }) { s ->
                    val course = courseById[s.courseId]
                    ItemCard(
                        tint = course?.let { Color(it.colorArgb) } ?: Color.Gray,
                        title = sessionDisplayTitle(s, course?.name),
                        lines = listOfNotNull(
                            course?.name?.takeIf { s.title.isNotBlank() },
                            durationText(s.totalSeconds),
                            if (s.completed) stringResource(R.string.session_completed) else null,
                        ),
                        onClick = { editing = s },
                        onDelete = { deleting = s },
                        extraAction = {
                            IconButton(onClick = { timer.start(s) }, enabled = !t.active) {
                                Icon(
                                    Icons.Filled.PlayArrow,
                                    contentDescription = stringResource(R.string.start),
                                    tint = if (t.active) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.primary,
                                )
                            }
                        },
                    )
                }
            }
        }
        AddButton({ creating = true }, Modifier.align(Alignment.BottomEnd))
    }

    if (creating) SessionForm(null, ui.courses, { creating = false }, viewModel::saveSession)
    editing?.let { s -> SessionForm(s, ui.courses, { editing = null }, viewModel::saveSession) }
    deleting?.let { s ->
        DeleteDialog(R.string.delete_item_msg, onConfirm = {
            if (t.snapshot.sessionId == s.id) timer.stop()
            viewModel.deleteSession(s.id)
        }, onDismiss = { deleting = null })
    }
    if (confirmStop) {
        AlertDialog(
            onDismissRequest = { confirmStop = false },
            title = { Text(stringResource(R.string.stop_timer_title)) },
            text = { Text(stringResource(R.string.stop_timer_msg)) },
            confirmButton = { TextButton(onClick = { timer.stop(); confirmStop = false }) { Text(stringResource(R.string.stop_timer_confirm)) } },
            dismissButton = { TextButton(onClick = { confirmStop = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}
