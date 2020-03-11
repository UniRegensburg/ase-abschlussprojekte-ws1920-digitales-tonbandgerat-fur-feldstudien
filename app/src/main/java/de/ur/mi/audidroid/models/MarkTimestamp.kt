package de.ur.mi.audidroid.models

import androidx.room.*

/** The MarkerEntity represents the table with the marks a user made.
 *  RecordingAndMarker maps the one-to-many relationship between a recording and its marks.
 * @author: Lisa Sanladerer
 */

@Entity(tableName = "markerTable")
data class MarkerEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "markerName") val markerName: String
)

@Entity(tableName = "markerTimeTable")
data class MarkTimestamp(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "mid") val mid: Int,
    @ColumnInfo(name = "recordingId") val recordingId: Int,
    @ColumnInfo(name = "markerId") val markerId: Int,
    @ColumnInfo(name = "markTime") val markTime: String
)

data class MarkAndTimestamp(
    @Embedded val marker: MarkerEntity,
    @Relation(
        parentColumn = "uid",
        entityColumn = "markerId"
    )
    val markTimestamp: MarkTimestamp
)
