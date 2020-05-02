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

    @Query("SELECT R.uid, R.recordingName, R.recordingPath, R.date, R.duration, GROUP_CONCAT(L.labelName,', ') AS labels FROM recordingsTable R LEFT JOIN labelAssignmentTable A ON R.uid = A.recordingId LEFT JOIN labelsTable L ON L.uid = A.labelId GROUP BY R.uid ORDER BY R.uid DESC")
    fun getAllRecordingsWithLabels(): LiveData<List<RecordingAndLabels>>

    @Query("SELECT R.uid, R.recordingName, R.recordingPath, R.date, R.duration, GROUP_CONCAT(L.labelName,', ') AS labels FROM recordingsTable R LEFT JOIN labelAssignmentTable A ON R.uid = A.recordingId LEFT JOIN labelsTable L ON L.uid = A.labelId GROUP BY R.uid ORDER BY CASE WHEN :isAsc = 1 THEN R.recordingName END ASC, CASE WHEN :isAsc = 0 THEN R.recordingName END DESC")
    fun getAllRecordingsWithLabelsOrderName(isAsc: Boolean): LiveData<List<RecordingAndLabels>>

    @Query("SELECT R.uid, R.recordingName, R.recordingPath, R.date, R.duration, GROUP_CONCAT(L.labelName,', ') AS labels FROM recordingsTable R LEFT JOIN labelAssignmentTable A ON R.uid = A.recordingId LEFT JOIN labelsTable L ON L.uid = A.labelId GROUP BY R.uid ORDER BY CASE WHEN :isAsc = 1 THEN R.duration END ASC, CASE WHEN :isAsc = 0 THEN R.duration END DESC")
    fun getAllRecordingsWithLabelsOrderDuration(isAsc: Boolean): LiveData<List<RecordingAndLabels>>

    @Query("SELECT R.uid, R.recordingName, R.recordingPath, R.date, R.duration, GROUP_CONCAT(L.labelName,', ') AS labels FROM recordingsTable R LEFT JOIN labelAssignmentTable A ON R.uid = A.recordingId LEFT JOIN labelsTable L ON L.uid = A.labelId GROUP BY R.uid ORDER BY CASE WHEN :isAsc = 1 THEN R.date END ASC, CASE WHEN :isAsc = 0 THEN R.date END DESC")
    fun getAllRecordingsWithLabelsOrderDate(isAsc: Boolean): LiveData<List<RecordingAndLabels>>

    @Query("SELECT * FROM labelsTable WHERE uid IN (SELECT DISTINCT(labelId) FROM labelAssignmentTable WHERE recordingId = :key)")
    fun getRecLabelsById(key: Int): LiveData<List<LabelEntity>>

    @Query("SELECT * FROM labelsTable WHERE labelName = :name")
    suspend fun getLabelByName(name: String): List<LabelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(labelEntity: LabelEntity)

    @Update
    suspend fun update(labelEntity: LabelEntity)

    @Delete
    suspend fun delete(labelEntity: LabelEntity)

    @Query("DELETE FROM labelsTable")
    suspend fun clearTable()
}
