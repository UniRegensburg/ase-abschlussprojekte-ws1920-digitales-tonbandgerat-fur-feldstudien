package de.ur.mi.audidroid.models

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import de.ur.mi.audidroid.utils.Converters
import java.util.concurrent.Executors
import kotlinx.coroutines.*

/**
 * The abstract class contains the database holder and serves as the main access point for the connection to the persisted data
 * @authors: Sabine Roth, Jonas Puchinger
 */

@Database(
    entities = [EntryEntity::class, MarkTimestamp::class, MarkerEntity::class, LabelEntity::class, LabelAssignmentEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)

abstract class RecorderDatabase : RoomDatabase() {

    abstract fun entryDao(): EntryDao
    abstract fun labelDao(): LabelDao
    abstract fun labelAssignmentDao(): LabelAssignmentDao
    abstract fun markerDao(): MarkerDao

    companion object {
        private var INSTANCE: RecorderDatabase? = null
        fun getInstance(context: Context?): RecorderDatabase {
            if (context != null) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context,
                        RecorderDatabase::class.java, "recorder-database"
                    ).addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Executors.newSingleThreadScheduledExecutor().execute {
                                val defaultMarker = MarkerEntity(0, "Mark")
                                val job = Job()
                                CoroutineScope(job + Dispatchers.Main).launch {
                                    getInstance(context).markerDao().insertMarker(defaultMarker)
                                }
                            }
                        }
                    })
                        .build()
                }
            }
            return INSTANCE as RecorderDatabase
        }
    }
}
