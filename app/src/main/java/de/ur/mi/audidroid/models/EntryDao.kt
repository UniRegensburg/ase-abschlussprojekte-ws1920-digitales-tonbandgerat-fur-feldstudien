package de.ur.mi.audidroid.models

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query


@Dao
interface EntryDao {
    @Query("SELECT * FROM recordingsTable")
    fun getAllRecordings(): List<EntryEntitiy>

    @Query("SELECT * FROM recordingsTable WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<EntryEntitiy>

    @Insert(onConflict = REPLACE)
    fun insert(entryEntitiy: EntryEntitiy)

    @Delete
    fun delete(entryEntitiy: EntryEntitiy)

    /** deletes all entries TODO: Delete comment after Issue #33 is done because clearTable is self-explaining*/
    @Query("DELETE FROM recordingsTable")
    fun clearTable()

    @Query("SELECT COUNT(recordingPath) FROM recordingsTable")
    fun getRowCount(): Int
}
