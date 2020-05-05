package de.ur.mi.audidroid.models

/**
 * RecordingAndLabels connects RecordingEntity and labels.
 * @author: Theresa Strohmeier
 */
data class RecordingAndLabels(
    val uid: Int,
    val recordingName: String,
    val recordingPath: String,
    val date: String,
    val duration: String,
    val labels: String?
)
