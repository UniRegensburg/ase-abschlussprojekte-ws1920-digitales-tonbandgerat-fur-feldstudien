package de.ur.mi.audidroid.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The RecordingEntity represents the table within the database and sets the field values
 * @author: Sabine Roth
 */

@Entity(tableName = "recordingsTable")
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "recordingName") var recordingName: String,
    @ColumnInfo(name = "recordingPath") val recordingPath: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "duration") val duration: String
)
