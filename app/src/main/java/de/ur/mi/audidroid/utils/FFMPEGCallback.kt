package de.ur.mi.audidroid.utils

import java.io.File

/**
 * Callback for file format conversion with FFMPEG, used in [AudioConverter].
 * @author: Jonas Puchinger
 * Adapted from: https://github.com/adrielcafe/AndroidAudioConverter
 */

interface FFMPEGCallback {

    fun onSuccess(convertedFile: File)

    fun onFailure(error: Exception)
}
