package de.ur.mi.audidroid.models

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

/**
 * The [MarkerDao] is the data access object to access the tables storing [MarkerEntity], [MarkTimestamp] and [MarkAndTimestamp] in the apps' room database.
 * Functions returning [LiveData] are incompatible with Kotlin coroutines, therefore these functions are not suspended.
 * @author: Jonas Puchinger
 * Adapted from: https://developer.android.com/training/data-storage/room/accessing-data
 */

@Dao
interface MarkerDao {

    @Query("SELECT * FROM markerTable")
    fun getAllMarkers(): LiveData<List<MarkerEntity>>

    @Query("SELECT COUNT(uid) FROM markerTable")
    suspend fun getMarkerCount(): Int

    @Query("SELECT * FROM markerTable WHERE markerName = :name")
    suspend fun getMarkerByName(name: String): List<MarkerEntity>

    @Insert(onConflict = REPLACE)
    suspend fun insertMarker(markerEntity: MarkerEntity)

    @Update
    suspend fun updateMarker(markerEntity: MarkerEntity)

    @Delete
    suspend fun deleteMarker(markerEntity: MarkerEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertMark(marker: MarkTimestamp)

    @Update
    suspend fun updateMarkTimestamp(markTimestamp: MarkTimestamp)

    @Query("DELETE FROM markerTimeTable WHERE recordingId = :copiedRecordingId AND markTimeInMilli NOT BETWEEN :startTimeInMilli AND :endTimeInMilli")
    suspend fun deleteOuterMarks(copiedRecordingId: Int, startTimeInMilli: Int, endTimeInMilli: Int)

    @Query("UPDATE markerTimeTable SET markTimeInMilli = markTimeInMilli - :startTimeInMilli WHERE recordingId = :copiedRecordingId")
    suspend fun updateInnerMarks(copiedRecordingId: Int, startTimeInMilli: Int)

    @Query("DELETE FROM markerTimeTable WHERE recordingId = :copiedRecordingId AND markTimeInMilli BETWEEN :startTimeInMilli AND :endTimeInMilli")
    suspend fun deleteInnerMarks(copiedRecordingId: Int, startTimeInMilli: Int, endTimeInMilli: Int)

    @Transaction
    @Query("SELECT DISTINCT R.recordingId, R.markerId, L.markerName FROM markerTimeTable R LEFT JOIN markerTable L ON R.markerId = L.uid")
    fun getRecordingsAndMarkerType(): LiveData<List<RecordingAndMarkTuple>>

    @Query("UPDATE markerTimeTable SET markTimeInMilli = markTimeInMilli - :durationInMilli WHERE recordingId = :copiedRecordingId AND markTimeInMilli > :durationInMilli")
    suspend fun updateOuterMarks(copiedRecordingId: Int, durationInMilli: Int)

    @Query("INSERT INTO markerTimeTable (mid, recordingId, markerId, markComment, markTimeInMilli) SELECT null, :copiedRecordingId, markerId, markComment, markTimeInMilli FROM markerTimeTable WHERE recordingId = :key")
    suspend fun copyMarks(key: Int, copiedRecordingId: Int)

    @Transaction
    @Query("SELECT DISTINCT * FROM markerTable INNER JOIN markerTimeTable ON markerTable.uid = markerTimeTable.markerId WHERE markerTimeTable.recordingId LIKE :key ORDER BY markTimeInMilli")
    fun getMarksById(key: Int): LiveData<List<MarkAndTimestamp>>

    @Query("DELETE FROM markerTimeTable WHERE recordingId = :key")
    suspend fun deleteRecMarks(key: Int)

    @Query("UPDATE markerTimeTable SET recordingId = :recordingId WHERE recordingId = :copiedRecordingId")
    suspend fun updateMarks(recordingId: Int, copiedRecordingId: Int)

    @Query("DELETE FROM markerTimeTable WHERE mid = :key")
    suspend fun deleteMark(key: Int)
}
