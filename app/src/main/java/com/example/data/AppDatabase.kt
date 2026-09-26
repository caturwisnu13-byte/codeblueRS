package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CodeBlueAlertEntity::class,
        IncidentLogEntity::class,
        ResponderEntity::class,
        HospitalRoomEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun codeBlueDao(): CodeBlueDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE code_blue_alerts ADD COLUMN roomId TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE code_blue_alerts ADD COLUMN deactivatedBy TEXT")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE responders ADD COLUMN isOnDuty INTEGER NOT NULL DEFAULT 1")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS hospital_rooms (
                            id TEXT PRIMARY KEY NOT NULL,
                            name TEXT NOT NULL,
                            building TEXT NOT NULL,
                            floor INTEGER NOT NULL,
                            defaultBed TEXT NOT NULL DEFAULT 'Bed 1',
                            posX REAL NOT NULL DEFAULT 0.5,
                            posY REAL NOT NULL DEFAULT 0.5,
                            isEmergencyPriority INTEGER NOT NULL DEFAULT 0,
                            isActive INTEGER NOT NULL DEFAULT 1
                        )
                    """.trimIndent())
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE responders ADD COLUMN isLeader INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "code_blue_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
