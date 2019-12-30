package de.ur.mi.audidroid.models


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordingsTable")
data class RecorderEntity (
    @PrimaryKey val uid: Int,
    @ColumnInfo (name= "recordingPath") val recordingPath: String,
    @ColumnInfo (name= "date") val date:  String,
    @ColumnInfo (name= "markName") val markName: String? = null,
    @ColumnInfo (name= "markTime") val markTime: String? = null //TODO: change to list (with @embedded and @parcelize for list option)
)
