package de.ur.mi.audidroid.viewmodels

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.RecorderDatabase
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * The ViewModel handles the changes to the view's data and the event logic for the user interaction referring to the RecordFragment
 * @author: Sabine Roth
 */

class RecordViewModel : ViewModel() {

    private var isRecording = false
    private var resumeRecord = false
    private val mediaRecorder: MediaRecorder = MediaRecorder()
    private var outputFile = ""
    private lateinit var db: RecorderDatabase


    fun initializeRecorder(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(context as Activity, permissions, 0)
            //TODO: check what happens if the permission is denied -> maybe a popup and closing the app?
        }

        outputFile =
            context.filesDir.absolutePath + "/recording.aac" //TODO: Change path to users preferred save location
        with(mediaRecorder) {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(outputFile)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        }

        try {
            mediaRecorder.prepare()
        } catch (e: IllegalStateException) {
        } catch (e: IOException) {
        }
    }

    fun recordPauseButtonClicked(button: ImageButton) =
        when (!isRecording) {
            true -> {
                button.setImageResource(R.mipmap.pause_button_foreground)
                isRecording = true
                if (!resumeRecord) {
                    startRecording()
                } else {
                    resumeRecording()
                }
            }

            false -> {
                button.setImageResource(R.mipmap.record_button_foreground)
                isRecording = false
                resumeRecord = true
                pauseRecording()
            }
        }

    private fun startRecording() {
        mediaRecorder.start()
    }

    private fun pauseRecording() {
        mediaRecorder.pause()
    }

    private fun resumeRecording() {
        mediaRecorder.resume()
    }

    fun cancelRecord(context: Context) {
        isRecording = false
        resumeRecord = false
        mediaRecorder.reset()
        sendToast(context, R.string.record_removed)
    }

    fun confirmRecord(context: Context) {
        isRecording = false
        resumeRecord = false
        mediaRecorder.stop()
        mediaRecorder.reset()
        sendToast(context, R.string.record_saved)
        getLastUID(context)
    }

    /** Furnishes the current number of entries in the table to set the unique id for the new entry */
    private fun getLastUID(context: Context) {
        db = RecorderDatabase.getInstance(context)
        doAsync {
            val count = db.entryDao().getRowCount()
            uiThread {
                saveRecordInDB(count)
            }
        }
    }

    private fun saveRecordInDB(count: Int) {
        val audio =
            EntryEntity(count, outputFile, getDate())
        doAsync {
            db.entryDao().insert(audio)
        }
    }

    /**
     * Returns the current date
     * Adapted from: https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
     */
    private fun getDate(): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
    }

    /** Creates a toast with the given [text] */
    private fun sendToast(context: Context, text: Int) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
}
