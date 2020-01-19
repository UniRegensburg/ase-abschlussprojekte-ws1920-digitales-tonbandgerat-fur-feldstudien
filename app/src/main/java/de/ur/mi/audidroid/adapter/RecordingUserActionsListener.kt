package de.ur.mi.audidroid.adapter

import android.view.View
import de.ur.mi.audidroid.models.EntryEntity

interface RecordingUserActionsListener {
    fun onRecordingClicked(entryEntity: EntryEntity)

    fun onButtonClicked(entryEntity: EntryEntity, view: View)
}