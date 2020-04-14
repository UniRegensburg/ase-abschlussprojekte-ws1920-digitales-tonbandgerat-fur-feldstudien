package de.ur.mi.audidroid.adapter

import de.ur.mi.audidroid.models.MarkerEntity
import de.ur.mi.audidroid.views.EditMarkersFragment

/**
 * Listener for MarkerItem in [EditMarkersFragment].
 * Listens for click on whole card, and click on delete button.
 * @author: Jonas Puchinger
 */

interface MarkerUserActionsListener {

    fun onMarkerClicked(markerEntity: MarkerEntity)

    fun onMarkerDeleteClicked(markerEntity: MarkerEntity)
}
