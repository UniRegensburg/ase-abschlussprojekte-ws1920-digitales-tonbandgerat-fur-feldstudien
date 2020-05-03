package de.ur.mi.audidroid.adapter

import android.view.View
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.models.RecordingAndLabels

interface RecordingAndFolderActionsListener {
    fun onRecordingClicked(recordingAndLabels: RecordingAndLabels)

    fun popUpRecording(recordingAndLabels: RecordingAndLabels, view: View)

    fun onFolderClicked(folder: FolderEntity)

    fun popUpFolder(folder: FolderEntity, view: View)
}
