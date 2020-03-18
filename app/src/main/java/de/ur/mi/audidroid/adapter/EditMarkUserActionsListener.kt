package de.ur.mi.audidroid.adapter

import android.view.View
import de.ur.mi.audidroid.models.MarkAndTimestamp

interface EditMarkUserActionsListener {
    fun onMarkClicked(mark: MarkAndTimestamp, view: View)

    fun onNewCommentClicked(mark: MarkAndTimestamp)

    fun onMarkDeleteClicked(mark: MarkAndTimestamp)
}
