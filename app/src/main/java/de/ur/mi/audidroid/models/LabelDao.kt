package de.ur.mi.audidroid.models

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * The LabelDao is the data access object to access a table in the apps' room database
 *
 * @author: Jonas Puchinger
 * Adapted from: https://developer.android.com/training/data-storage/room/accessing-data
 */

@Dao
interface LabelDao {
    @Query("SELECT * FROM labelsTable")
    fun getAllLabels(): LiveData<List<LabelEntity>>

    @Query("SELECT * FROM labelsTable WHERE uid = :key")
    fun getLabelWithId(key: Int): LiveData<LabelEntity>

    @Query("SELECT * FROM labelsTable WHERE labelName IN (:name)")
    fun getLabelByName(name: String): LabelEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(labelEntity: LabelEntity)

    @Delete
    fun delete(labelEntity: LabelEntity)

    @Query("DELETE FROM labelsTable")
    fun clearTable()
}