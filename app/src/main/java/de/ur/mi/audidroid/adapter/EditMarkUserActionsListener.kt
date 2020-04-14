package de.ur.mi.audidroid.adapter

import android.view.View
import de.ur.mi.audidroid.models.ExpandableMarkAndTimestamp
import de.ur.mi.audidroid.models.MarkAndTimestamp
import de.ur.mi.audidroid.views.EditRecordingFragment

/**
 * Listener for EditMarkItem in [EditRecordingFragment].
 * Listens for click on whole card, click on comment button, and click on delete button.
 * @author: Theresa Strohmeier, Jonas Puchinger
 */

interface EditMarkUserActionsListener {

    fun onMarkClicked(mark: ExpandableMarkAndTimestamp, view: View)

    fun onEditCommentClicked(mark: ExpandableMarkAndTimestamp, view: View)

    fun onMarkDeleteClicked(mark: MarkAndTimestamp)
}
