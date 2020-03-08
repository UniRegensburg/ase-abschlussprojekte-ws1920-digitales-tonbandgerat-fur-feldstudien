package de.ur.mi.audidroid.models

import androidx.room.Embedded
import androidx.room.Relation

data class RecordingAndMarker(
    @Embedded val entryEntity: EntryEntity,
    @Relation(
        parentColumn = "uid",
        entityColumn = "recordingId"
    )
    val markList: List<MarkerTimeRelation>
)
