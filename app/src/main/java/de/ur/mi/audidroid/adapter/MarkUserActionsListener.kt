package de.ur.mi.audidroid.adapter

import de.ur.mi.audidroid.models.MarkerTimeRelation

interface MarkUserActionsListener {
    fun onMarkClicked(markerEntity: MarkerTimeRelation)
}
