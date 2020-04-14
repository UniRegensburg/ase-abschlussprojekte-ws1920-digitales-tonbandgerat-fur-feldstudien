package de.ur.mi.audidroid.adapter

import android.view.View
import de.ur.mi.audidroid.models.ExpandableMarkAndTimestamp
import de.ur.mi.audidroid.views.PlayerFragment

/**
 * Listener for MarkItem in [PlayerFragment].
 * Listens for clicks on whole card.
 * @author: Theresa Strohmeier, Jonas Puchinger
 */

interface MarkUserActionsListener {

    fun onMarkClicked(mark: ExpandableMarkAndTimestamp, view: View)
}
