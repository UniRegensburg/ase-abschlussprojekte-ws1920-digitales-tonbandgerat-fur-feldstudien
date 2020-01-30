package de.ur.mi.audidroid.models

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
    fun getAllRecordings(): List<EntryEntity>

    @Query("SELECT * FROM recordingsTable WHERE uid IN (:uniqueId)")
    fun loadEntryById(uniqueId: Int): EntryEntity

    @Insert(onConflict = REPLACE)
    fun insert(entryEntity: EntryEntity)

    @Insert
    fun insertMark(marker: MarkerEntity)

    @Transaction
    @Query("SELECT * FROM recordingsTable")
    fun getRecordingWithMarks(): List<MarkerToAudio>

    @Delete
    fun delete(entryEntity: EntryEntity)

    /** deletes all entries TODO: Delete comment after Issue #33 is done because clearTable is self-explaining*/
    @Query("DELETE FROM recordingsTable")
    fun clearTable()
}
