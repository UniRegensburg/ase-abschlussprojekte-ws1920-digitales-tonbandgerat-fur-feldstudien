package de.ur.mi.audidroid.models

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

/**
 * The EntryDao is the data access object  to access the app's data using the room persistence library
 * The DAO provide methods that offer abstract access to the app's database
 *
 * @author: Sabine Roth
 * Adapted from: https://developer.android.com/training/data-storage/room/accessing-data
 */

@Dao
interface EntryDao {
    @Query("SELECT * FROM recordingsTable")
    fun getAllRecordings(): LiveData<List<EntryEntity>>

    @Query("SELECT * FROM recordingsTable WHERE uid = :key")
    fun getRecordingWithId(key: Int): LiveData<EntryEntity>

    @Query("SELECT * FROM recordingsTable WHERE recordingName IN (:name)")
    fun getRecordingByName(name: String): EntryEntity

    @Insert(onConflict = REPLACE)
    fun insert(entryEntity: EntryEntity):Long

    @Delete
    fun delete(entryEntity: EntryEntity)

    @Query("DELETE FROM recordingsTable")
    fun clearTable()


    @Insert(onConflict = REPLACE)
    fun insertMark(marker: MarkerEntity)

    @Transaction
    @Query("SELECT * FROM recordingsTable WHERE uid = :key")
    fun getRecordingInclMarks(key: Int): List<RecordingAndMarker>

    @Query("SELECT * FROM markerTable")
    fun getAllMarks(): List<MarkerEntity>
}
