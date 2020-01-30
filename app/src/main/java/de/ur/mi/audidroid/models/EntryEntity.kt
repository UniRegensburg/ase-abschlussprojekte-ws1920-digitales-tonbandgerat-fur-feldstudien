package de.ur.mi.audidroid.models


import androidx.room.*

/**
 * The EntryEntity represents the table within the database and sets the field values
 * @author: Sabine Roth
 *
 * The MarkerEntity contains all highlighted Instances. The table MarkerToAudio maps the
 * one-to-many relationship between the recording and its marks.
 * Adapted from https://developer.android.com/training/data-storage/room/relationships
 * @author Lisa Sanladerer
 */


@Entity
data class MarkerEntity(
    @PrimaryKey(autoGenerate = true) val mid: Int,
    val recordingId: Int,
    val labels: String,
    val markTime: String
)

data class MarkerToAudio(
    @Embedded val recording: EntryEntity,
    @Relation(
        parentColumn = "uid",
        entityColumn = "recordingId"
    )
    val markList: List<MarkerEntity>
)

@Entity(tableName = "recordingsTable")
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "recordingPath") val recordingPath: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "duration") val duration: String
)
