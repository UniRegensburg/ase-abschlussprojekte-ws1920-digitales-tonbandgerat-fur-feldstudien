package de.ur.mi.audidroid.models

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import de.ur.mi.audidroid.BR

/**
 * Extension class to make [MarkAndTimestamp] keep track of expanded UI state.
 * Uses [Bindable] decorator.
 * @author: Jonas Puchinger
 */

class ExpandableMarkAndTimestamp(val markAndTimestamp: MarkAndTimestamp) : BaseObservable() {

    @get:Bindable
    var isExpanded: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.expanded)
        }
}
