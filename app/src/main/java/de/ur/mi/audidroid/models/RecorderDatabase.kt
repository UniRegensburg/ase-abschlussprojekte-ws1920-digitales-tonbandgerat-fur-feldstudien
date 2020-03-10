package de.ur.mi.audidroid.models

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.ur.mi.audidroid.utils.Converters

/**
 * The abstract class contains the database holder and serves as the main access point for the connection to the persisted data
 * @authors: Sabine Roth, Jonas Puchinger
 */

@Database(
    entities = [EntryEntity::class, FolderEntity::class, MarkerTimeRelation::class, LabelEntity::class, LabelAssignmentEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RecorderDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
    abstract fun labelDao(): LabelDao
    abstract fun labelAssignmentDao(): LabelAssignmentDao
    abstract fun markerDao(): MarkerDao
    abstract fun folderDao() : FolderDao

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
