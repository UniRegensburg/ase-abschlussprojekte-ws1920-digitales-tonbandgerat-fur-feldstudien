package de.ur.mi.audidroid.adapter

import android.view.View
import de.ur.mi.audidroid.models.ExpandableMarkAndTimestamp

interface MarkUserActionsListener {

    fun onMarkClicked(mark: ExpandableMarkAndTimestamp, view: View)

}
