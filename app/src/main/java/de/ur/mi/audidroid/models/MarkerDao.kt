package de.ur.mi.audidroid.models

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface MarkerDao {

    @Query("SELECT * FROM markerTable")
    fun getAllMarkers() : LiveData<List<MarkerEntity>>

    @Query("SELECT COUNT(uid) FROM markerTable")
    suspend fun getMarkerCount(): Int

    @Query("SELECT * FROM markerTable WHERE uid = :key")
    fun getMarkerById(key: Int): LiveData<MarkerEntity>

    @Query("SELECT * FROM markerTable WHERE markerName IN (:name)")
    fun getMarkerByName(name: String): LiveData<MarkerEntity>

    @Insert(onConflict = REPLACE)
    suspend fun insertMarker(markerEntity: MarkerEntity)

    @Update
    suspend fun updateMarker(markerEntity: MarkerEntity)

    @Delete
    suspend fun deleteMarker(markerEntity: MarkerEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertMark(marker: MarkTimestamp)

    @Transaction
    @Query("SELECT * FROM markerTable")
    fun getMarkAndTimestamp(): LiveData<List<MarkAndTimestamp>>

    @Transaction
    @Query("SELECT * FROM markerTimeTable INNER JOIN markerTable ON markerTimeTable.markerId=markerTable.uid WHERE markerTimeTable.recordingId = :key")
    fun getMarksById(key: Int): LiveData<List<MarkAndTimestamp>>

    @Transaction
    @Query("SELECT * FROM recordingsTable WHERE uid = :key IN (SELECT DISTINCT(mid) FROM markerTimeTable)")
    fun getRecordingFromIdInclMarks(key: Int): LiveData<List<RecordingAndMarker>>

    @Query("SELECT * FROM markerTimeTable WHERE recordingId = :key")
    fun allMarks(key: Int): LiveData<List<MarkTimestamp>>

    @Query("DELETE FROM markerTimeTable WHERE recordingId = :key")
    suspend fun deleteRecMarks(key: Int)

    @Query("DELETE FROM markerTimeTable WHERE mid = :key")
    suspend fun deleteMark(key: Int)
}
