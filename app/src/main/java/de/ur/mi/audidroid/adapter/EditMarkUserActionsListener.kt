package de.ur.mi.audidroid.adapter

import android.view.View
import de.ur.mi.audidroid.models.ExpandableMarkAndTimestamp
import de.ur.mi.audidroid.models.MarkAndTimestamp

interface EditMarkUserActionsListener {
    fun onMarkClicked(mark: ExpandableMarkAndTimestamp, view: View)

    fun onEditCommentClicked(mark: ExpandableMarkAndTimestamp, view: View)

    fun onMarkDeleteClicked(mark: MarkAndTimestamp)

    fun onMarkTimeClicked(mark: MarkAndTimestamp)
}
