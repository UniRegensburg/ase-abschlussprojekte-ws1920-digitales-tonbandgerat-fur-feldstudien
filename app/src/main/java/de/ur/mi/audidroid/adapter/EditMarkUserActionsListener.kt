package de.ur.mi.audidroid.adapter

import de.ur.mi.audidroid.models.MarkAndTimestamp

interface EditMarkUserActionsListener {
    fun onMarkClicked(mark: MarkAndTimestamp)

    fun onMarkDeleteClicked(mark: MarkAndTimestamp)
}
