package de.ur.mi.audidroid.adapter

import android.view.View
import de.ur.mi.audidroid.models.RecordingAndLabels
import de.ur.mi.audidroid.views.FilesFragment

/**
 * Listener for RecordingItem in [FilesFragment].
 * Listens for clicks on whole card, and on popup menu button.
 * @author: Theresa Strohmeier, Jonas Puchinger
 */

interface RecordingUserActionsListener {

    fun onRecordingClicked(recordingAndLabels: RecordingAndLabels)

    fun onButtonClicked(recordingAndLabels: RecordingAndLabels, view: View)
}
