package de.ur.mi.audidroid.models

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE

/**
 * DAO for the LabelAssignmentEntity
 *
 * @author: Sabine Roth
 * Adapted from: https://developer.android.com/training/data-storage/room/accessing-data
 */

@Dao
interface LabelAssignmentDao {

    @Insert(onConflict = REPLACE)
    fun insertRecLabels(labelAssignmentEntity: LabelAssignmentEntity)
}
