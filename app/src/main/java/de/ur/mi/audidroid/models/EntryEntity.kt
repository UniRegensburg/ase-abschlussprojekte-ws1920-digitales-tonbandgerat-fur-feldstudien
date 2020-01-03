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
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "recordingPath") val recordingPath: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "markName") val markName: String? = null,
    @ColumnInfo(name = "markTime") val markTime: String? = null //TODO: @lisa: change to list (with @embedded and @parcelize for list option because MutableList and ArrayList are not allowed data types)
)
