package de.ur.mi.audidroid.adapter

import de.ur.mi.audidroid.models.MarkerTimeRelation

interface EditMarkUserActionsListener {
    fun onMarkClicked(markerEntity: MarkerTimeRelation)

    fun onMarkDeleteClicked(markerEntity: MarkerTimeRelation)
}
