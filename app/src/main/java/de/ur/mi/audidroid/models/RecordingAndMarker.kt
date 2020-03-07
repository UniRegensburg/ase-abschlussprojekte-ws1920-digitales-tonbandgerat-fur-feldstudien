package de.ur.mi.audidroid.models

import androidx.room.Embedded
import androidx.room.Relation

/**
 * This class captures the relationship between [EntryEntity] and [MarkerEntity], which is
 * used by Room to fetch the related entities.
 */
data class RecordingAndMarker(
    @Embedded val entryEntity: EntryEntity,
    @Relation(
        parentColumn = "uid",
        entityColumn = "recordingId"
    )
    val markList: List<MarkerEntity>
)

