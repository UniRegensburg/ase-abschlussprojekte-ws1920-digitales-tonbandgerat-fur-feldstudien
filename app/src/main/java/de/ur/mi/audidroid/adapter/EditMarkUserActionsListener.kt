package de.ur.mi.audidroid.adapter

import de.ur.mi.audidroid.models.MarkTimestamp

interface EditMarkUserActionsListener {
    fun onMarkClicked(markerEntity: MarkTimestamp)

    fun onMarkDeleteClicked(markerEntity: MarkTimestamp)
}
