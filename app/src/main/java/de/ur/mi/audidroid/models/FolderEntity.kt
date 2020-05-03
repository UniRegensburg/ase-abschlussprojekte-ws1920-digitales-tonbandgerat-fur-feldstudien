package de.ur.mi.audidroid.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The [FolderEntity] represents the folders for the recordings.
 * @author Sabine Roth
 */

@Entity(tableName = "foldersTable")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "folderName") val folderName: String
)
