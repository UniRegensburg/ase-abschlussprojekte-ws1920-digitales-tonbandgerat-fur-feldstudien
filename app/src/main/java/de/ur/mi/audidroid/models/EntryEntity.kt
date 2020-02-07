package de.ur.mi.audidroid.models

import androidx.room.*

/**
 * The EntryEntity represents the table within the database and sets the field values
 * @author: Sabine Roth
 */

@Entity(tableName = "recordingsTable")
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "recordingPath") val recordingPath: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "duration") val duration: String,
    @ColumnInfo(name = "markName") val markName: String? = null,
    @ColumnInfo(name = "labels") val labels: String? = null // TODO: change to list
)