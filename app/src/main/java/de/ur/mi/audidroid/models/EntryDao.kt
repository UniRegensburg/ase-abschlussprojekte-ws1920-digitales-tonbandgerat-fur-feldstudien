package de.ur.mi.audidroid.models

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query


@Dao
interface EntryDao {
    @Query("SELECT * FROM recordingsTable")
    fun getAllRecordings(): List<RecorderEntity>

    @Query("SELECT * FROM recordingsTable WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<RecorderEntity>

    @Insert(onConflict = REPLACE)
    fun insert(recorderEntity: RecorderEntity)

    @Delete
    fun delete(recorderEntity: RecorderEntity)

    //deletes all entries
    @Query("DELETE FROM recordingsTable")
    fun clearTable()

    @Query("SELECT COUNT(recordingPath) FROM recordingsTable")
    fun getRowCount(): Int

}
