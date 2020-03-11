package de.ur.mi.audidroid.models

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * The LabelDao is the data access object to access the table storing labels in the apps' room database.
 * Functions returning LiveData are incompatible wit Kotlin coroutines, therefore these functions are not suspended.
 *
 * @author: Jonas Puchinger
 * Adapted from: https://developer.android.com/training/data-storage/room/accessing-data
 */

@Dao
interface LabelDao {

    @Query("SELECT * FROM labelsTable")
    fun getAllLabels(): LiveData<List<LabelEntity>>

    @Query("SELECT * FROM labelsTable WHERE uid = :key")
    fun getLabelById(key: Int): LiveData<LabelEntity>

    @Query("SELECT * FROM labelsTable WHERE labelName IN (:name)")
    fun getLabelByName(name: String): LiveData<LabelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(labelEntity: LabelEntity)

    @Update
    suspend fun update(labelEntity: LabelEntity)

    @Delete
    suspend fun delete(labelEntity: LabelEntity)

    @Query("DELETE FROM labelsTable")
    suspend fun clearTable()
}
