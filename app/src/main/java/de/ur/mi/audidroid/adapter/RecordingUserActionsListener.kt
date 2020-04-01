package de.ur.mi.audidroid.adapter

import android.view.View
import de.ur.mi.audidroid.models.RecordingAndLabels

interface RecordingUserActionsListener {

    fun onRecordingClicked(
        recordingAndLabels: RecordingAndLabels
    )

    fun onButtonClicked(
        recordingAndLabels: RecordingAndLabels,
        view: View
    )
}
