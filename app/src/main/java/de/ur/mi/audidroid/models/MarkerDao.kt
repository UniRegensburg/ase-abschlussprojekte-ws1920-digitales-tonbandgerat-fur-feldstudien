package de.ur.mi.audidroid.models

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface MarkerDao {

    @Query("SELECT * FROM markerTable")
    fun getAllMarkers() : LiveData<List<MarkerEntity>>

    @Query("SELECT * FROM markerTable WHERE uid = :key")
    fun getMarkerById(key: Int): LiveData<MarkerEntity>

    @Query("SELECT * FROM markerTable WHERE markerName IN (:name)")
    fun getMarkerByName(name: String): LiveData<MarkerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarker(markerEntity: MarkerEntity)

    @Update
    suspend fun updateMarker(markerEntity: MarkerEntity)

    @Delete
    suspend fun deleteMarker(markerEntity: MarkerEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertMark(marker: MarkerTimeRelation)

    @Transaction
    @Query("SELECT * FROM recordingsTable WHERE uid = :key")
    fun getRecordingFromIdInclMarks(key: Int): List<RecordingAndMarker>

    @Query("SELECT * FROM markerTimeTable")
    fun getAllMarks(): List<MarkerTimeRelation>
}
