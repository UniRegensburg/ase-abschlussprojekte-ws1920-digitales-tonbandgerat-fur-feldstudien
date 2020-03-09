package de.ur.mi.audidroid.models

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface MarkerDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertMark(marker: MarkerTimeRelation)

    @Transaction
    @Query("SELECT * FROM recordingsTable WHERE uid = :key IN (SELECT DISTINCT(mid) FROM markerTimeTable)")
    fun getRecordingFromIdInclMarks(key: Int): LiveData<List<RecordingAndMarker>>

    @Query("SELECT * FROM markerTimeTable WHERE recordingId = :key")
    fun getAllMarks(key: Int): LiveData<List<MarkerTimeRelation>>

    @Query("DELETE FROM markerTimeTable WHERE recordingId = :key")
    suspend fun deleteRecMarks(key: Int)

    @Query("DELETE FROM markerTimeTable WHERE mid = :key")
    suspend fun deleteMark(key: Int)
}
