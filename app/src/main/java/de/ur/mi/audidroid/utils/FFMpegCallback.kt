package de.ur.mi.audidroid.utils

import java.io.File

interface FFMpegCallback {

    fun onSuccess(convertedFile: File, startTime: String, endTime: String)

    fun onFailure(error: Exception)
}
