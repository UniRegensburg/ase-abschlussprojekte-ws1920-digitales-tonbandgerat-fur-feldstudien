package de.ur.mi.audidroid.models

import androidx.room.Embedded
import androidx.room.Relation

/**
 *  RecordingAndMarker maps the one-to-many relationship between a recording and its marks.
 * @author: Lisa Sanladerer
 */

data class RecordingAndMarker(
    @Embedded val entryEntity: EntryEntity,
    @Relation(
        parentColumn = "uid",
        entityColumn = "recordingId"
    )
    val markList: List<MarkerTimeRelation>
)
