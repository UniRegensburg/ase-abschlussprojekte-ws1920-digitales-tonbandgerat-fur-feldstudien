package de.ur.mi.audidroid.models

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

/**
 * DAO for the [FolderAssignmentEntity]
 * @author: Sabine Roth
 */

@Dao
interface FolderAssignmentDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertFolderAssignment(folderAssignmentEntity: FolderAssignmentEntity)

    @Query("DELETE FROM folderAssignmentTable WHERE recordingId = :key")
    suspend fun deleteFolderAssignment(key: Int)
}
