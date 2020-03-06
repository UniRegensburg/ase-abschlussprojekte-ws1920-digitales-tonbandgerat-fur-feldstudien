package de.ur.mi.audidroid.models

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction

/**
 * DAO for the LabelAssignmentEntity
 *
 * @author: Sabine Roth
 * Adapted from: https://developer.android.com/training/data-storage/room/accessing-data
 */

@Dao
interface LabelAssignmentDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertRecLabels(labelAssignmentEntity: LabelAssignmentEntity)

    @Transaction
    @Query("SELECT * FROM recordingsTable WHERE uid = :key")
    fun getRecordingFromIdInclLabels(key: Int): List<RecordingAndLabel>
}
