package de.ur.mi.audidroid.adapter

import de.ur.mi.audidroid.models.MarkerEntity
import de.ur.mi.audidroid.models.MarkerTimeRelation

interface MarkerUserActionsListener {

    fun onMarkerClicked(markerEntity: MarkerEntity)

    fun onMarkerDeleteClicked(markerEntity: MarkerEntity)

}
