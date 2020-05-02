package de.ur.mi.audidroid.utils

import java.io.File

interface FFMpegCallback {

    fun onSuccess(convertedFile: File, type: String, startTimeInMilli: Int, endTimeInMilli: Int)

    fun onFailure(error: Exception)
}
