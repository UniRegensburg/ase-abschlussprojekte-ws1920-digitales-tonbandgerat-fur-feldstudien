package de.ur.mi.audidroid.models

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface MarkerDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertMark(marker: MarkerTimeRelation)

    @Transaction
    @Query("SELECT * FROM recordingsTable WHERE uid = :key")
    fun getRecordingFromIdInclMarks(key: Int): List<RecordingAndMarker>

    @Query("SELECT * FROM markerTimeTable")
    fun getAllMarks(): List<MarkerTimeRelation>
}