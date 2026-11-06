package app.planner.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import app.planner.data.local.dao.*
import app.planner.data.local.entity.*

@Database(
    entities = [
        CourseEntity::class, StudyEventEntity::class, StudySessionEntity::class,
        ExamEntity::class, AssignmentEntity::class,
        NotificationRecordEntity::class, AppSettingEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun studyEventDao(): StudyEventDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun examDao(): ExamDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun notificationRecordDao(): NotificationRecordDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        /** One explicit Migration per schema bump. Destructive fallback is deliberately NOT enabled. */
        val MIGRATIONS: Array<Migration> = emptyArray()

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "planner.db")
                .addMigrations(*MIGRATIONS)
                .build()
    }
}
