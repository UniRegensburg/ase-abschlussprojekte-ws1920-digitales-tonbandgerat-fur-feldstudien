package de.ur.mi.audidroid.adapter

import de.ur.mi.audidroid.models.CombinedMarkAndTimestamp
import de.ur.mi.audidroid.models.MarkAndTimestamp

interface EditMarkUserActionsListener {
    fun onMarkClicked(mark: CombinedMarkAndTimestamp)

    fun onMarkDeleteClicked(mark: CombinedMarkAndTimestamp)
}
