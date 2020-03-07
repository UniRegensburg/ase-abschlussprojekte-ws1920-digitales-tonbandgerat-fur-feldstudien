package de.ur.mi.audidroid.adapter

import de.ur.mi.audidroid.models.MarkerEntity

interface MarkUserActionsListener {
    fun onMarkClicked(markerEntity: MarkerEntity)
}
