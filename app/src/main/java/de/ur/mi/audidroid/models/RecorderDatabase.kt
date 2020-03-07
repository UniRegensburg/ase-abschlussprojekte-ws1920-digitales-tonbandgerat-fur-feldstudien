package de.ur.mi.audidroid.models

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

/**
 * The abstract class contains the database holder and serves as the main access point for the connection to the persisted data
 * @authors: Sabine Roth, Jonas Puchinger
 */


@Database(entities = [EntryEntity::class, MarkerTimeRelation::class, MarkerEntity::class, LabelEntity::class], version = 1, exportSchema = false)
abstract class RecorderDatabase : RoomDatabase() {

    abstract fun entryDao(): EntryDao
    abstract fun labelDao(): LabelDao
    abstract fun markerDao(): MarkerDao

    private val job = Job()
    private val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    companion object {
        private var INSTANCE: RecorderDatabase? = null
        fun getInstance(context: Context?): RecorderDatabase {
            if (context != null) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context,
                        RecorderDatabase::class.java, "recorder-database"
                    ).build()
                    INSTANCE!!.populateInitialData()
                }
            }
            return INSTANCE as RecorderDatabase
        }
    }

    private fun populateInitialData() {
        val defaultMarker = MarkerEntity(0, "Mark")
        CoroutineScope(coroutineContext).launch {
            markerDao().insertMarker(defaultMarker)
        }
    }

}
