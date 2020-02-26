package de.ur.mi.audidroid.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import cafe.adriel.androidaudioconverter.AndroidAudioConverter
import cafe.adriel.androidaudioconverter.callback.IConvertCallback
import cafe.adriel.androidaudioconverter.model.AudioFormat
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import java.io.File
import java.lang.Exception
import java.lang.IllegalArgumentException

object ShareHelper {

    fun shareAudio(recording: EntryEntity, context: Context) {
        convertFile(recording.recordingPath, context)
    }

    fun convertFile(filename: String, context: Context) {
        val file = File(filename)

        val cb = object: IConvertCallback {
            override fun onSuccess(convertedFile: File?) {
                shareFile(convertedFile!!, context)
            }

            override fun onFailure(error: Exception?) {
                Log.e("File Conversion", "$error")
            }
        }

        AndroidAudioConverter.with(context)
            .setFile(file)
            .setFormat(AudioFormat.MP3)
            .setCallback(cb)
            .convert()
    }

    fun getUriFromFile(file: File, context: Context): Uri {
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
        val uri = getUriFromFile(file, context)
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "audio/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent: Intent = Intent.createChooser(shareIntent, context.resources.getString(R.string.share_recording))
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(chooserIntent)
    }

}
