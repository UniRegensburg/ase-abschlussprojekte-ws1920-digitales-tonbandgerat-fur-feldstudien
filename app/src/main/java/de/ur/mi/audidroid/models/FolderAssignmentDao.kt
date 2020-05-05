package de.ur.mi.audidroid.models

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

/**
 * DAO for the [FolderAssignmentEntity]
 * @author: Sabine Roth, Theresa Strohmeier, Jonas Puchinger
 */

@Dao
interface FolderAssignmentDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertFolderAssignment(folderAssignmentEntity: FolderAssignmentEntity)

    @Query("DELETE FROM folderAssignmentTable WHERE recordingId = :recordingId")
    suspend fun deleteFolderAssignment(recordingId: Int)

    @Query("SELECT * FROM folderAssignmentTable WHERE recordingId = :recId")
    suspend fun getFolderOfRecording(recId: Int): FolderAssignmentEntity

    @Query("SELECT R.uid, R.recordingName, R.recordingPath, R.date, R.duration, GROUP_CONCAT(L.labelName,', ') AS labels FROM recordingsTable R LEFT JOIN labelAssignmentTable A ON R.uid = A.recordingId LEFT JOIN labelsTable L ON L.uid = A.labelId LEFT JOIN folderAssignmentTable F ON R.uid = F.recordingId WHERE F.folderId = :folderId GROUP BY R.uid")
    fun getAllRecordingsOfFolder(folderId: Int): LiveData<List<RecordingAndLabels>>

    @Query("SELECT R.uid, R.recordingName, R.recordingPath, R.date, R.duration, GROUP_CONCAT(L.labelName,', ') AS labels FROM recordingsTable R LEFT JOIN labelAssignmentTable A ON R.uid = A.recordingId LEFT JOIN labelsTable L ON L.uid = A.labelId LEFT JOIN folderAssignmentTable F ON R.uid = F.recordingId WHERE F.folderId IS NULL GROUP BY R.uid")
    fun getAllRecordingsOutsideFolder(): LiveData<List<RecordingAndLabels>>
}
