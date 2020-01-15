package de.ur.mi.audidroid.viewmodels

import android.content.Context
import android.media.MediaRecorder
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Chronometer
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.RecorderDatabase
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class RecordViewModel : ViewModel() {

    private var isRecording = false
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

    fun recordPauseButtonClicked(button: ImageButton, context: Context) =
        when (!isRecording) {
            true -> {
                button.setImageResource(R.mipmap.pause_button_foreground)
                isRecording = true
                if (!resumeRecord) {
                    resetTimer()
                    timer.start()
                    initializeRecorder(context)
                    startRecording()
                } else {
                    timer.base = SystemClock.elapsedRealtime() - getStoppedTime(context)
                    timer.start()
                    resumeRecording()
                }
            }

            false -> {
                button.setImageResource(R.mipmap.record_button_foreground)
                isRecording = false
                resumeRecord = true
                pauseRecording()
                timer.stop()
                currentRecordTime = timer.text.toString()
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

    fun cancelRecord(view: View) {
        showSnackBar(view, R.string.record_removed)
        endRecordSession()
    }

    fun confirmRecord(view: View, context: Context) {
        showSnackBar(view, R.string.record_saved)
        saveRecordInDB(context)
        endRecordSession()
    }

    private fun endRecordSession() {
        isRecording = false
        resumeRecord = false
        mediaRecorder.stop()
        mediaRecorder.reset()
        resetTimer()
    }

    private fun saveRecordInDB(context: Context) {
        db = RecorderDatabase.getInstance(context)
        val audio =
            EntryEntity(0, outputFile, getDate(), timer.text.toString())
        doAsync {
            db.entryDao().insert(audio)
        }
    }

    private fun getDate(): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
    }

    private fun showSnackBar(view: View, text: Int) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).show()
    }

    fun initializeTimer(chronometer: Chronometer) {
        timer = chronometer
    }

    /** Returns the last stopped time as an Integer value */
    private fun getStoppedTime(context: Context): Int {
        val timeArray = currentRecordTime.split(":")
        val res = context.resources
        return if (timeArray.size == 2) {
            (Integer.parseInt(timeArray[0]) * res.getInteger(R.integer.counter_divider_minutes_hours) * res.getInteger(
                R.integer.counter_multiplier
            )) + (Integer.parseInt(timeArray[1]) * res.getInteger(R.integer.counter_multiplier))
        } else {
            (Integer.parseInt(timeArray[0]) * res.getInteger(R.integer.counter_divider_minutes_hours) * res.getInteger(
                R.integer.counter_divider_minutes_hours
            ) * res.getInteger(R.integer.counter_multiplier)) + (Integer.parseInt(timeArray[1]) * res.getInteger(
                R.integer.counter_divider_minutes_hours
            ) * res.getInteger(R.integer.counter_multiplier)) + (Integer.parseInt(
                timeArray[2]
            ) * res.getInteger(R.integer.counter_multiplier))
        }
    }

    /** Resets timer to 00:00 */
    private fun resetTimer() {
        timer.stop()
        timer.base = SystemClock.elapsedRealtime()
    }
}
