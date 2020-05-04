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

    @Query("DELETE FROM folderAssignmentTable WHERE recordingId = :key")
    suspend fun deleteFolderAssignment(key: Int)

    @Query("UPDATE folderAssignmentTable SET folderId = :folderId WHERE primaryKey = :key")
    suspend fun updateFolderAssignment(key: Int, folderId: Int)

    @Query("SELECT * FROM folderAssignmentTable WHERE recordingId = :recId")
    suspend fun getFolderOfRecording(recId: Int): FolderAssignmentEntity
}
