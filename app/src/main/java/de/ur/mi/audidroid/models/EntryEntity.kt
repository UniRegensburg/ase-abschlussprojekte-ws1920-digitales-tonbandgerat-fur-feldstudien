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

/** The MarkerEntity represents the table with the marks a user made.
 *  RecordingAndMarker maps the one-to-many relationship between a recording and its marks.
 *
 */

@Entity(tableName = "markerTable")
data class MarkerEntity(
    @PrimaryKey(autoGenerate = true) val mid: Int,
    val recordingId: Int,
    val markName: String,
    val markTime: String
)

data class RecordingAndMarker(
    @Embedded val entryEntity: EntryEntity,
    @Relation(
        parentColumn = "uid",
        entityColumn = "recordingId"
    )
    val markList: List<MarkerEntity>
)