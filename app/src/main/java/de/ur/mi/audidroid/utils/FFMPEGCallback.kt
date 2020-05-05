package de.ur.mi.audidroid.utils

import java.io.File

interface FFmpegCallback {

    fun onSuccess(convertedFile: File, type: String, startTimeInMilli: Int, endTimeInMilli: Int)

    fun onFailure(error: Exception)
}
