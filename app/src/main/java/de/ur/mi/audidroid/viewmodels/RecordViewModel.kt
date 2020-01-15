package de.ur.mi.audidroid.viewmodels

import android.content.Context
import android.media.MediaRecorder
import android.os.SystemClock
import android.widget.Chronometer
import android.widget.Toast
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


    private var resumeRecord = false
    private val mediaRecorder: MediaRecorder = MediaRecorder()
    private var outputFile = ""
    private lateinit var db: RecorderDatabase
    private lateinit var timer: Chronometer
    private var currentRecordTime: String = ""

    private fun initializeRecorder(context: Context) {
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

    fun recordButtonClicked(context: Context) {
        when (resumeRecord) {
            true -> {
                timer.base = SystemClock.elapsedRealtime() - getStoppedTime()
                timer.start()
                resumeRecording()
            }
            false -> {
                timer.start()
                initializeRecorder(context)
                startRecording()
            }
        }
    }

    fun pauseButtonClicked() {
        resumeRecord = true
        pauseRecording()
        timer.stop()
        currentRecordTime = timer.text.toString()
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
        resumeRecord = false
        mediaRecorder.reset()
        resetTimer()
        sendToast(context, R.string.record_removed)
    }

    fun confirmRecord(context: Context) {
        resumeRecord = false
        mediaRecorder.stop()
        mediaRecorder.reset()
        resetTimer()
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

    fun initializeTimer(chronometer: Chronometer) {
        timer = chronometer
    }

    /** Returns the last stopped time as an Integer value */
    private fun getStoppedTime(): Int {
        val timeArray = currentRecordTime.split(":")
        return if (timeArray.size == 2) {
            (Integer.parseInt(timeArray[0]) * 60 * 1000) + (Integer.parseInt(timeArray[1]) * 1000)
        } else {
            (Integer.parseInt(timeArray[0]) * 60 * 60 * 1000) + (Integer.parseInt(timeArray[1]) * 60 * 1000) + (Integer.parseInt(
                timeArray[2]
            ) * 1000)
        }
    }

    /** Resets timer to 00:00 */
    private fun resetTimer() {
        timer.stop()
        timer.base = SystemClock.elapsedRealtime()
    }
}
