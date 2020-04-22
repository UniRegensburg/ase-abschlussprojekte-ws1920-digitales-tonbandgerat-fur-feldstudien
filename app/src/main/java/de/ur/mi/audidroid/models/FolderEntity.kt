package de.ur.mi.audidroid.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.ur.mi.audidroid.R

@Entity(tableName = "foldersTable")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "folderName") val folderName: String,
    @ColumnInfo(name = "dirPath") val dirPath: String? = null,
    @ColumnInfo(name = "isExternal") val isExternal: Boolean = false,
    @ColumnInfo(name = "parentDir") val parentDir: Int? = null,
    @ColumnInfo(name = "nestingDescr") val nestingDescr: String? = null,
    @ColumnInfo(name = "content") val content: String = "",
    @ColumnInfo(name = "contentCount") val contentCount: Int = 0
)