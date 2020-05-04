package de.ur.mi.audidroid.models

import androidx.room.*

/**
 * The [FolderAssignmentEntity] represents the folders referring to the recording.
 * The folders are saved with their unique ids in case of changing the name of the folder.
 *
 * @author Sabine Roth
 */

@Entity(tableName = "folderAssignmentTable")
data class FolderAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val primaryKey: Int,
    @ColumnInfo(name = "recordingId") val recordingId: Int,
    @ColumnInfo(name = "folderId") val folderId: Int
)
