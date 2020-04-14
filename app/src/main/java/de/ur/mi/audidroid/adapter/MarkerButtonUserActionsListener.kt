package de.ur.mi.audidroid.adapter

import de.ur.mi.audidroid.models.MarkerEntity
import de.ur.mi.audidroid.views.EditRecordingFragment
import de.ur.mi.audidroid.views.RecordFragment

/**
 * Listener for MarkerButton in [RecordFragment] and [EditRecordingFragment].
 * Listens for click on the button.
 * @author: Jonas Puchinger
 */

interface MarkerButtonUserActionsListener {

    fun onMarkerButtonClicked(markerEntity: MarkerEntity)
}
