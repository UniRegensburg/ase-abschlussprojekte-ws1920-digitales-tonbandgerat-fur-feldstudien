package de.ur.mi.audidroid.viewmodels

import android.content.Context
import android.media.MediaRecorder
import android.os.SystemClock
import android.widget.Chronometer
import android.widget.FrameLayout
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.databinding.RecordFragmentBinding
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.RecorderDatabase
import org.jetbrains.anko.doAsync
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * The ViewModel handles the changes to the view's data and the event logic for the user interaction referring to the RecordFragment
 * @author: Sabine Roth
 */

class RecordViewModel(val context: Context, private val binding: RecordFragmentBinding) :
    ViewModel() {


    private var resumeRecord = false
    private val mediaRecorder: MediaRecorder = MediaRecorder()
    private var outputFile = ""
    private lateinit var db: RecorderDatabase
    private lateinit var timer: Chronometer
    private var currentRecordTime: String = ""
    private lateinit var frameLayout: FrameLayout

    init {
        binding.buttonsVisible = false
        binding.isRecording = false
    }

    fun initializeTimer(chronometer: Chronometer) {
        timer = chronometer
    }

    fun initializeLayout(frameLayout: FrameLayout) {
        this.frameLayout = frameLayout
    }

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

    fun recordPauseButtonClicked() {
        when (binding.isRecording) {
            false -> {
                recordButtonClicked()
                binding.buttonsVisible = true
                binding.isRecording = true
            }
            true -> {
                pauseButtonClicked()
                binding.isRecording = false
            }
        }
    }

    private fun recordButtonClicked() {
        when (resumeRecord) {
            true -> {
                timer.base = SystemClock.elapsedRealtime() - getStoppedTime(context)
                timer.start()
                resumeRecording()
            }
            false -> {
                resetTimer()
                timer.start()
                initializeRecorder(context)
                startRecording()
            }
        }
    }

    private fun pauseButtonClicked() {
        resumeRecord = true
        pauseRecording()
        timer.stop()
        currentRecordTime = timer.text.toString()
    }

    fun cancelRecord() {
        showSnackBar(R.string.record_removed)
        endRecordSession()
    }

    fun confirmRecord() {
        showSnackBar(R.string.record_saved)
        saveRecordInDB()
        endRecordSession()
    }

    private fun endRecordSession() {
        binding.buttonsVisible = false
        binding.isRecording = false
        resumeRecord = false
        mediaRecorder.stop()
        mediaRecorder.reset()
        resetTimer()
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

    private fun saveRecordInDB() {
        db = RecorderDatabase.getInstance(context)
        val audio =
            EntryEntity(0, outputFile, getDate(), timer.text.toString())
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

    /** Sends a snackbar for user information with the given [text] */
    private fun showSnackBar(text: Int) {
        Snackbar.make(frameLayout, text, Snackbar.LENGTH_LONG).show()
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
