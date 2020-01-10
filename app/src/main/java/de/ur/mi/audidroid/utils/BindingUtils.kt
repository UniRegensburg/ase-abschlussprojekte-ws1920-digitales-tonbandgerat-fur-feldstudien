package de.ur.mi.audidroid.utils

import android.widget.TextView
import androidx.databinding.BindingAdapter
import de.ur.mi.audidroid.models.EntryEntity

@BindingAdapter("recordingDate")
fun TextView.setRecordingDate(item: EntryEntity?) {
    item?.let {
        text = item.date
    }
}