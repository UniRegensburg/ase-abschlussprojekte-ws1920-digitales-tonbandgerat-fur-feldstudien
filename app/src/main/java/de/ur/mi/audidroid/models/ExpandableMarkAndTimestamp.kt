package de.ur.mi.audidroid.models

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import de.ur.mi.audidroid.BR

class ExpandableMarkAndTimestamp(val markAndTimestamp: MarkAndTimestamp) : BaseObservable() {

    @get:Bindable
    var isExpanded: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.expanded)
        }

}
