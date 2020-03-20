package de.ur.mi.audidroid.adapter

import android.view.View
import de.ur.mi.audidroid.models.LabelDao

interface RecordingUserActionsListener {
    fun onRecordingClicked(recordingAndLabels: LabelDao.RecordingAndLabels)

    fun onButtonClicked(recordingAndLabels: LabelDao.RecordingAndLabels, view: View)
}
