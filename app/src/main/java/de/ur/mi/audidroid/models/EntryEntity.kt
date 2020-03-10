package de.ur.mi.audidroid.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The EntryEntity represents the table within the database and sets the field values
 * @author: Sabine Roth
 */

@Entity(tableName = "recordingsTable")
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "recordingName") val recordingName: String,
    @ColumnInfo(name = "recordingPath") val recordingPath: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "duration") val duration: String,
    @ColumnInfo(name = "folder") val folder: Int? = null,
    @ColumnInfo(name = "labels") val labels: String? = null
)
