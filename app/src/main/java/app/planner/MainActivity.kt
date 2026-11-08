package app.planner

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.planner.domain.model.ThemeMode
import app.planner.ui.AppRoot
import app.planner.ui.settings.SettingsViewModel
import app.planner.ui.study.StudyViewModel
import app.planner.ui.study.TimerViewModel
import app.planner.ui.theme.AppTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as PlannerApp).container
        setContent {
            val settingsVm: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(container.settingsRepository))
            val studyVm: StudyViewModel = viewModel(factory = StudyViewModel.factory(container.studyRepository))
            val timerVm: TimerViewModel = viewModel(factory = TimerViewModel.factory(container.timerStore, container.studyRepository))
            val mode by settingsVm.themeMode.collectAsStateWithLifecycle()
            val dark = when (mode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            AppTheme(dark) { AppRoot(settingsVm, studyVm, timerVm) }
        }
    }
}
