package de.ur.mi.audidroid.models

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

/**
 * The RecordingDao is the data access object  to access the app's data using the room persistence library
 * The DAO provide methods that offer abstract access to the app's database
 *
 * @author: Sabine Roth
 * Adapted from: https://developer.android.com/training/data-storage/room/accessing-data
 */

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recordingsTable")
    fun getAllRecordings(): LiveData<List<RecordingEntity>>

    @Query("SELECT * FROM recordingsTable WHERE uid = :key")
    fun getRecordingById(key: Int): LiveData<RecordingEntity>

    @Insert(onConflict = REPLACE)
    suspend fun insert(recordingEntity: RecordingEntity): Long

    @Query("DELETE FROM recordingsTable WHERE uid = :key")
    suspend fun delete(key: Int)

}
