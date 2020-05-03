package de.ur.mi.audidroid.models

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import androidx.room.TypeConverters
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.utils.Converters

/**
 * The abstract class contains the database holder and serves as the main access point for the connection to the persisted data
 * @authors: Sabine Roth, Jonas Puchinger
 */

@Database(
    entities = [RecordingEntity::class, MarkTimestamp::class, MarkerEntity::class, LabelEntity::class, LabelAssignmentEntity::class, FolderEntity::class, FolderAssignmentEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)

abstract class RecorderDatabase : RoomDatabase() {

    abstract fun recordingDao(): RecordingDao
    abstract fun labelDao(): LabelDao
    abstract fun labelAssignmentDao(): LabelAssignmentDao
    abstract fun markerDao(): MarkerDao
    abstract fun folderDao(): FolderDao
    abstract fun folderAssignmentDao(): FolderAssignmentDao

    companion object {
        private var INSTANCE: RecorderDatabase? = null
        fun getInstance(context: Context?): RecorderDatabase {
            if (context != null) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context,
                        RecorderDatabase::class.java, "recorder-database"
                    ).addCallback(object: RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Executors.newSingleThreadScheduledExecutor().execute {
                                val defaultMarkerQuestion = MarkerEntity(0, context.getString(R.string.default_marker_question))
                                val defaultMarkerAnswer = MarkerEntity(0, context.getString(R.string.default_marker_answer))
                                val job: CompletableJob = Job()
                                CoroutineScope(job + Dispatchers.Main).launch {
                                    getInstance(context).markerDao().insertMarker(defaultMarkerQuestion)
                                    getInstance(context).markerDao().insertMarker(defaultMarkerAnswer)
                                }
                            }

                            Executors.newSingleThreadScheduledExecutor().execute {
                                val defaultLabelInterview = LabelEntity(0, context.getString(R.string.default_label_interview))
                                val job: CompletableJob = Job()
                                CoroutineScope(job + Dispatchers.Main).launch {
                                    getInstance(context).labelDao().insert(defaultLabelInterview)
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
