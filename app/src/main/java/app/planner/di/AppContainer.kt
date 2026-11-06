package app.planner.di

import android.content.Context
import app.planner.data.local.db.AppDatabase
import app.planner.data.repository.RoomDeviceIdProvider
import app.planner.data.repository.StudyRepository
import app.planner.data.settings.SettingsRepository
import app.planner.data.settings.settingsDataStore
import app.planner.domain.repository.DeviceIdProvider

/** Manual dependency container (no DI framework: lean build, fully offline). */
class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.build(context)
    val studyRepository = StudyRepository(database)
    val settingsRepository = SettingsRepository(context.applicationContext.settingsDataStore)
    val deviceIdProvider: DeviceIdProvider = RoomDeviceIdProvider(database.appSettingDao())
}
