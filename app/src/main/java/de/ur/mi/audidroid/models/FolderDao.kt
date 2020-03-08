package de.ur.mi.audidroid.models

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FolderDao {

    @Query("SELECT * FROM foldersTable")
    fun getAllFolders(): LiveData<List<FolderEntity>>

    @Query("SELECT * FROM foldersTable WHERE uid = :key")
    fun getFolderById(key: Int): LiveData<FolderEntity>

    @Query("SELECT * FROM foldersTable WHERE folderName IN (:name)")
    fun getFolderByName(name: String): LiveData<FolderEntity>

    @Query("SELECT * FROM foldersTable WHERE folderName IN (:path)")
    fun getFolderByPath(path: String): LiveData<FolderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folderEntity: FolderEntity): Long

    @Update
    suspend fun update(folderEntity: FolderEntity)

    @Delete
    suspend fun delete(folderEntity: FolderEntity)

    @Query("DELETE FROM foldersTable")
    suspend fun clearTable()

    @Update
    suspend fun update(entryEntity: EntryEntity)
}
