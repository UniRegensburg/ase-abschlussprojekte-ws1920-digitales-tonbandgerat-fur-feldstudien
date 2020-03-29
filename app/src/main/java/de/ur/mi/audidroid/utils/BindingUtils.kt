package de.ur.mi.audidroid.utils

import android.text.format.DateUtils
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.chip.Chip
import de.ur.mi.audidroid.models.RecordingAndLabels

@BindingAdapter("label1")
fun Chip.setLabel1(recording: RecordingAndLabels) {
    recording.let {
        if (recording.labels != null) {
            if (recording.labels.contains(",")) {
                val labels = recording.labels.split(",")
                text = labels[0]
            } else {
                text = recording.labels
            }
        } else {
            visibility = View.GONE
        }
    }
}

@BindingAdapter("label2")
fun Chip.setLabel2(recording: RecordingAndLabels) {
    recording.let {
        if (recording.labels != null) {
            if (recording.labels.contains(",")) {
                val labels = recording.labels.split(",")
                text = labels[1]
            } else {
                visibility = View.GONE
            }
        } else {
            visibility = View.GONE
        }
    }
}

@BindingAdapter("label3")
fun Chip.setLabel3(recording: RecordingAndLabels) {
    recording.let {
        if (recording.labels != null) {
            if (recording.labels.contains(",")) {
                val labels = recording.labels.split(",")
                if (labels.size > 2) {
                    text = labels[2]
                } else {
                    visibility = View.GONE
                }
            } else {
                visibility = View.GONE
            }
        } else {
            visibility = View.GONE
        }
    }
}

@BindingAdapter("markTime")
fun TextView.setMarkTime(markTimeInMilli: Int) {
    markTimeInMilli.let {
        val markTimeInSec = markTimeInMilli / 1000
        text = DateUtils.formatElapsedTime(markTimeInSec.toLong())
    }
}

