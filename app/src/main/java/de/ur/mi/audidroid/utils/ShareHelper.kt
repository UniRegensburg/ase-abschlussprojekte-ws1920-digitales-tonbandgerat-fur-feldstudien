package de.ur.mi.audidroid.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import java.io.File
import java.lang.IllegalArgumentException

object ShareHelper {

    fun shareAudio(recording: EntryEntity, context: Context) {
        val uri = getUriFromFilename(recording.recordingPath, context)

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

    fun getUriFromFilename(filename: String, context: Context): Uri {
        val file = File(filename)
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

}
