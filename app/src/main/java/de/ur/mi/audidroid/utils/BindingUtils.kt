package de.ur.mi.audidroid.utils

import android.content.Context
import android.content.res.ColorStateList
import android.text.format.DateUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.RecordingAndLabels

@BindingAdapter("labels")
fun setLabels(view: ChipGroup, recording: RecordingAndLabels) {
    view.removeAllViews()
    if (recording.labels == null) {
        view.visibility = View.GONE
    } else {
        val context: Context = view.context
        if (recording.labels.contains(",")) {
            val labels = recording.labels.split(",")
            for (label in labels) {
                view.addView(createChip(context, label))
            }
        } else {
            view.addView(createChip(context, recording.labels))
        }
    }
}

private fun createChip(context: Context, name: String): Chip {
    val chip = Chip(context)
    with(chip) {
        text = name
        isClickable = false
        chipBackgroundColor =
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.chip_background))
    }
    return chip
}

@BindingAdapter("markTime")
fun TextView.setMarkTime(markTimeInMilli: Int) {
    markTimeInMilli.let {
        val markTimeInSec = markTimeInMilli / 1000
        text = DateUtils.formatElapsedTime(markTimeInSec.toLong())
    }
}

@BindingAdapter("buttonText")
fun Button.setButtonText(isExpanded: Boolean) {
    isExpanded.let {
        text = if (it) {
            context.resources.getString(R.string.show_markers_button)
        } else {
            context.resources.getString(R.string.hide_markers_button)
        }
    }
}
