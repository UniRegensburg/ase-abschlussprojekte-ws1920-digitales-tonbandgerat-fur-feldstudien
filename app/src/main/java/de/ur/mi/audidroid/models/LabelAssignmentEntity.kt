package de.ur.mi.audidroid.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * The LabelAssignmentEntity represents the labels referring to the recording.
 * The relation link the unique id of the recording to the entry.
 * The labels are saved with their unique ids in case of changing the name of the label.
 *
 * @author Sabine Roth
 */

@Entity(tableName = "labelAssignmentTable")
data class LabelAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val primaryKey: Int,
    @ColumnInfo(name = "recordingId") val recordingId: Int,
    @Relation(
        parentColumn = "uid",
        entityColumn = "recordingId"
    )
    @ColumnInfo(name = "listLabelId") val listLabelId: ArrayList<Int>
)
