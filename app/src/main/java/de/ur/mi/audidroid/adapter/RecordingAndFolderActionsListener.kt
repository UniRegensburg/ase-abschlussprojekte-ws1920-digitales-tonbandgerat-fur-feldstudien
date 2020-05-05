package de.ur.mi.audidroid.adapter

import android.view.View
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.models.RecordingAndLabels
import de.ur.mi.audidroid.views.FilesFragment

/**
 * Listener for RecordingItem in [FilesFragment].
 * Listens for clicks on whole card, and on popup menu button.
 * @author: Theresa Strohmeier, Jonas Puchinger
 */

interface RecordingAndFolderActionsListener {

    fun onRecordingClicked(recordingAndLabels: RecordingAndLabels)

    fun popUpRecording(recordingAndLabels: RecordingAndLabels, view: View)

    fun onFolderClicked(folder: FolderEntity)

    fun popUpFolder(folder: FolderEntity, view: View)
}
