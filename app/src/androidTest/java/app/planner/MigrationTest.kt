package app.planner

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.planner.data.local.db.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @Test fun migration1To2AddsSecondsAndKeepsExistingSessions() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(null) // in-memory
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE TABLE study_sessions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, courseId INTEGER, " +
                            "title TEXT NOT NULL, dateEpochDay INTEGER NOT NULL, startMinute INTEGER NOT NULL, " +
                            "durationMinutes INTEGER NOT NULL, notes TEXT, completed INTEGER NOT NULL, " +
                            "createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, deletedAt INTEGER)"
                    )
                    db.execSQL(
                        "INSERT INTO study_sessions (title, dateEpochDay, startMinute, durationMinutes, completed, createdAt, updatedAt) " +
                            "VALUES ('Old session', 20000, 600, 45, 0, 1, 1)"
                    )
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            })
            .build()
        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        val db = helper.writableDatabase

        AppDatabase.MIGRATION_1_2.migrate(db)

        db.query("SELECT title, durationMinutes, extraSeconds FROM study_sessions").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("Old session", c.getString(0))
            assertEquals(45, c.getInt(1))
            assertEquals(0, c.getInt(2))
        }
        helper.close()
    }
}
