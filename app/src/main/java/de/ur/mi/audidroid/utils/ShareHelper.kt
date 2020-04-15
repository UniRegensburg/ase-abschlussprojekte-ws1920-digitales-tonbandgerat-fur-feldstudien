package de.ur.mi.audidroid.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import cafe.adriel.androidaudioconverter.callback.IConvertCallback
import cafe.adriel.androidaudioconverter.model.AudioFormat
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.RecordingAndLabels
import java.io.File
import java.lang.Exception
import java.lang.IllegalArgumentException

/**
 * Utility class to handle the process of converting recordings to other file formats and sharing them to other apps.
 * [AudioConverter] is used for conversion. Available formats: AAC, MP3, M4A, WMA, WAV, FLAC.
 * @author: Jonas Puchinger
 */

object ShareHelper {

    fun shareAudio(recording: RecordingAndLabels, convertFormat: String, context: Context) {
        convertFile(recording.recordingPath, convertFormat, context)
    }

    private fun convertFile(filename: String, convertFormat: String, context: Context) {
        val file = File(filename)

        val cb = object : IConvertCallback {
            override fun onSuccess(convertedFile: File?) {
                shareFile(convertedFile!!, context)
            }

            override fun onFailure(error: Exception?) {
                Log.e("File Conversion", "$error")
            }
        }

        val converter = AudioConverter()
        with(converter) {
            setFile(file)
            setFormat(
                when (convertFormat) {
                    context.resources.getString(R.string.audio_format_mp3) -> AudioFormat.MP3
                    context.resources.getString(R.string.audio_format_aac) -> AudioFormat.AAC
                    context.resources.getString(R.string.audio_format_m4a) -> AudioFormat.M4A
                    context.resources.getString(R.string.audio_format_wma) -> AudioFormat.WMA
                    context.resources.getString(R.string.audio_format_wav) -> AudioFormat.WAV
                    context.resources.getString(R.string.audio_format_flac) -> AudioFormat.FLAC
                    else -> AudioFormat.MP3
                }
            )
            setCallback(cb)
            convert()
        }
    }

    private fun getUriFromFile(file: File, context: Context): Uri {
        val fileUri: Uri? = try {
            FileProvider.getUriForFile(
                context,
                "de.ur.mi.audidroid.fileprovider",
                file
            )
        } catch (e: IllegalArgumentException) {
            Log.e("File Selector", "File can't be shared: $file")
            null
        }
        return fileUri!!
    }

    fun shareFile(file: File, context: Context) {
        val uri: Uri = getUriFromFile(file, context)
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "audio/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent: Intent =
            Intent.createChooser(shareIntent, context.resources.getString(R.string.share_recording))
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(chooserIntent)
    }
}
