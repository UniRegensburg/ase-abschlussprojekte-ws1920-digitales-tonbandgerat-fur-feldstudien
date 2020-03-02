package de.ur.mi.audidroid.utils

import java.io.File

interface FFMpegCallback {

    fun onSuccess(convertedFile: File, type: String)

    fun onFailure(error: Exception)

    fun onNotAvailable(error: Exception)

    fun onFinish()
}
