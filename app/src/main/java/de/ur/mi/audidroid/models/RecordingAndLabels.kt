package de.ur.mi.audidroid.models

data class RecordingAndLabels(
    val uid: Int,
    val recordingName: String,
    val recordingPath: String,
    val date: String,
    val duration: String,
    val labels: String?,
    val folder: Int?
)
