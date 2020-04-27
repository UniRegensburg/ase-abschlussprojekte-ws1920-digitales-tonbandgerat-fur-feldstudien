package de.ur.mi.audidroid.adapter

import android.view.View
import de.ur.mi.audidroid.models.ExpandableMarkAndTimestamp
import de.ur.mi.audidroid.models.MarkAndTimestamp

interface MarkUserActionsListener {

    fun onMarkClicked(mark: ExpandableMarkAndTimestamp, view: View)

    fun onMarkTimeClicked(mark: MarkAndTimestamp)
}
