package de.ur.mi.audidroid.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The LabelEntity represents the table within the database, that stores the pre-defined labels assignable to individual recordings.
 * @author: Jonas Puchinger
 */

@Entity(tableName = "labelsTable")
data class LabelEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "labelName") val labelName: String
)
