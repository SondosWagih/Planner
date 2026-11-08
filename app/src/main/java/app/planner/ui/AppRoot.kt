package app.planner.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import android.content.Context
import android.media.RingtoneManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.planner.R
import app.planner.ui.navigation.Routes
import app.planner.ui.navigation.StudyDestination
import app.planner.ui.settings.SettingsScreen
import app.planner.ui.settings.SettingsViewModel
import app.planner.ui.study.AssignmentsScreen
import app.planner.ui.study.CoursesScreen
import app.planner.ui.study.ExamsScreen
import app.planner.ui.study.PlannerScreen
import app.planner.ui.study.ScheduleScreen
import app.planner.ui.study.StudyViewModel
import app.planner.ui.study.TimerViewModel
import app.planner.ui.study.sessionDisplayTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(settingsViewModel: SettingsViewModel, studyViewModel: StudyViewModel, timerViewModel: TimerViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val inSettings = route == Routes.SETTINGS
    val title = StudyDestination.entries.firstOrNull { it.route == route }?.label
        ?: if (inSettings) R.string.settings else R.string.tab_schedule

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(title)) },
                navigationIcon = {
                    if (inSettings) IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    if (!inSettings) IconButton(onClick = { nav.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Outlined.Settings, stringResource(R.string.settings))
                    }
                },
            )
        },
        bottomBar = {
            if (!inSettings) NavigationBar {
                StudyDestination.entries.forEach { dest ->
                    NavigationBarItem(
                        selected = route == dest.route,
                        onClick = {
                            nav.navigate(dest.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = null) },
                        label = { Text(stringResource(dest.label)) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(nav, startDestination = StudyDestination.SCHEDULE.route, modifier = Modifier.padding(padding)) {
            composable(StudyDestination.SCHEDULE.route) { ScheduleScreen(studyViewModel) }
            composable(StudyDestination.COURSES.route) { CoursesScreen(studyViewModel) }
            composable(StudyDestination.EXAMS.route) { ExamsScreen(studyViewModel) }
            composable(StudyDestination.ASSIGNMENTS.route) { AssignmentsScreen(studyViewModel) }
            composable(StudyDestination.PLANNER.route) { PlannerScreen(studyViewModel, timerViewModel) }
            composable(Routes.SETTINGS) { SettingsScreen(settingsViewModel) }
        }
    }
    // A finished timer is announced on any screen.
    val finishedId by timerViewModel.finishedSessionId.collectAsStateWithLifecycle()
    val studyState by studyViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(finishedId) { if (finishedId != null) playDoneSound(context) }
    finishedId?.let { id ->
        val session = studyState.sessions.firstOrNull { it.id == id }
        val courseName = studyState.courses.firstOrNull { it.id == session?.courseId }?.name
        val title = session?.let { sessionDisplayTitle(it, courseName) } ?: stringResource(R.string.session_default_title)
        AlertDialog(
            onDismissRequest = { timerViewModel.dismissFinished() },
            title = { Text(stringResource(R.string.timer_done_title)) },
            text = { Text(stringResource(R.string.timer_done_msg, title)) },
            confirmButton = { TextButton(onClick = { timerViewModel.dismissFinished() }) { Text(stringResource(R.string.ok)) } },
        )
    }
}

private fun playDoneSound(context: Context) {
    runCatching {
        RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))?.play()
    }
}
