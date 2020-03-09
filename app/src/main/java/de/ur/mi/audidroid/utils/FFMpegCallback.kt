package de.ur.mi.audidroid.utils

import java.io.File

interface FFMpegCallback {

    fun onSuccess(convertedFile: File)

    fun onFailure(error: Exception)
}
