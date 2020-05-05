package de.ur.mi.audidroid.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The LabelAssignmentEntity represents the labels referring to the recording.
 * The labels are saved with their unique ids in case of changing the name of the label.
 * @author Sabine Roth
 */

@Entity(tableName = "labelAssignmentTable")
data class LabelAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val primaryKey: Int,
    @ColumnInfo(name = "recordingId") val recordingId: Int,
    @ColumnInfo(name = "labelId") val labelId: Int
)
