package de.ur.mi.audidroid.adapter

import de.ur.mi.audidroid.models.LabelEntity

interface LabelUserActionsListener {
    fun onLabelClicked(labelEntity: LabelEntity)

    fun onLabelDeleteClicked(labelEntity: LabelEntity)
}