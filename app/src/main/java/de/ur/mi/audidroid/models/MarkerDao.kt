package de.ur.mi.audidroid.models

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface MarkerDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertMark(marker: MarkerEntity)

    @Transaction
    @Query("SELECT * FROM recordingsTable WHERE uid = :key")
    fun getRecordingFromIdInclMarks(key: Int): LiveData<List<RecordingAndMarker>>

    @Query("SELECT * FROM markerTimeTable")
    fun getAllMarks(): List<MarkerEntity>

    @Query("DELETE FROM markerTimeTable WHERE recordingId = :key")
    suspend fun deleteRecMarks(key: Int)
}
