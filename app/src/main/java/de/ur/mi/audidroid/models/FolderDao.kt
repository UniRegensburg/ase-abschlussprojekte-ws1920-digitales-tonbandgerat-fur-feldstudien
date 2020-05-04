package de.ur.mi.audidroid.models

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * DAO for the [FolderEntity]
 * @author: Sabine Roth
 */

@Dao
interface FolderDao{

    @Query("SELECT * FROM foldersTable")
    fun getAllFolders(): LiveData<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folderEntity: FolderEntity)

    @Update
    suspend fun updateFolder(folderEntity: FolderEntity)

    @Delete
    suspend fun deleteFolder(folderEntity: FolderEntity)
}
