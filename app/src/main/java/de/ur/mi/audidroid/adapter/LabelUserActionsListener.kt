package de.ur.mi.audidroid.adapter

import de.ur.mi.audidroid.models.LabelEntity
import de.ur.mi.audidroid.views.EditLabelsFragment

/**
 * Listener for LabelItem in [EditLabelsFragment].
 * Listens for click on whole card, and click on delete button.
 * @author: Jonas Puchinger
 */

interface LabelUserActionsListener {

    fun onLabelClicked(labelEntity: LabelEntity)

    fun onLabelDeleteClicked(labelEntity: LabelEntity)
}
