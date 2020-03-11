package de.ur.mi.audidroid.adapter

import de.ur.mi.audidroid.models.MarkerEntity

interface MarkerUserActionsListener {

    fun onMarkerClicked(markerEntity: MarkerEntity)

    fun onMarkerDeleteClicked(markerEntity: MarkerEntity)

}
