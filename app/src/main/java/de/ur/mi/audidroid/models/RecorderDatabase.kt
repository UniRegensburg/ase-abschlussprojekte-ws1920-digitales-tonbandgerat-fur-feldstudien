package de.ur.mi.audidroid.models

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(RecorderEntity::class), version = 1, exportSchema = false)
abstract class RecorderDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao

    companion object {
        private var INSTANCE: RecorderDatabase? = null
        fun getInstance(context: Context?): RecorderDatabase {
            if (context != null) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context,
                        RecorderDatabase::class.java, "recorder-database"
                    ).build()
                }
            }
            return INSTANCE as RecorderDatabase
        }
    }
}
