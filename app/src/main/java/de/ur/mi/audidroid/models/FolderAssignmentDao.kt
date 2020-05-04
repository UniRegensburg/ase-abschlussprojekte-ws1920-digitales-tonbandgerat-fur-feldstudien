package de.ur.mi.audidroid.models

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction

/**
 * DAO for the [FolderAssignmentEntity]
 * @author: Sabine Roth
 */

@Dao
interface FolderAssignmentDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertFolderAssignment(folderAssignmentEntity: FolderAssignmentEntity)

    @Transaction
    @Query("SELECT DISTINCT L.* FROM folderAssignmentTable R LEFT JOIN recordingsTable L ON R.recordingId = L.uid WHERE R.folderId = :key")
    suspend fun getRecordingsOfFolder(key: Int): List<RecordingEntity>

    @Query("DELETE FROM folderAssignmentTable WHERE recordingId = :recordingId")
    suspend fun deleteFolderAssignment(recordingId: Int)

    @Query("UPDATE folderAssignmentTable SET folderId = :folderId WHERE primaryKey = :key")
    suspend fun updateFolderAssignment(key: Int, folderId: Int)

    @Query("SELECT * FROM folderAssignmentTable WHERE recordingId = :recId")
    suspend fun getFolderOfRecording(recId: Int): FolderAssignmentEntity

    @Query("SELECT R.uid, R.recordingName, R.recordingPath, R.date, R.duration, GROUP_CONCAT(L.labelName,', ') AS labels FROM recordingsTable R LEFT JOIN labelAssignmentTable A ON R.uid = A.recordingId LEFT JOIN labelsTable L ON L.uid = A.labelId LEFT JOIN folderAssignmentTable F ON R.uid = F.recordingId WHERE F.folderId = :folderId GROUP BY R.uid")
    fun getAllRecordingsOfFolder(folderId: Int): LiveData<List<RecordingAndLabels>>
}
