package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.os.SystemClock
import android.text.format.DateUtils
import android.widget.Chronometer
import android.widget.FrameLayout
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.Repository
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * The ViewModel handles the changes to the view's data and the event logic for the user interaction referring to the RecordFragment
 * @author: Sabine Roth
 */

class RecordViewModel(private val dataSource: Repository, application: Application) :
    AndroidViewModel(application) {

    private var resumeRecord = false
    private val mediaRecorder: MediaRecorder = MediaRecorder()
    private var outputFile = ""
    private lateinit var timer: Chronometer
    private var currentRecordTime: String = ""
    private lateinit var frameLayout: FrameLayout
    private var recorderInitialized = false
    private val context = getApplication<Application>().applicationContext
    var isRecording = MutableLiveData<Boolean>()
    var buttonsVisible = MutableLiveData<Boolean>()
    val res = context.resources

    init {
        isRecording.value = false
        buttonsVisible.value = false
    }

    fun initializeTimer(chronometer: Chronometer) {
        timer = chronometer
    }

    fun initializeLayout(frameLayout: FrameLayout) {
        this.frameLayout = frameLayout
    }

    private fun initializeRecorder() {
        outputFile =
            context.filesDir.absolutePath + "/recording.aac" // TODO: Change path to users preferred save location
        with(mediaRecorder) {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(outputFile)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        }
        try {
            mediaRecorder.prepare()
            recorderInitialized = true
        } catch (e: IllegalStateException) {
            showSnackBar(R.string.error_message_recorder_initialization)
        } catch (e: IOException) {
            showSnackBar(R.string.error_message_recorder_file)
        }
    }

    fun recordPauseButtonClicked() {
        when (isRecording.value) {
            false -> {
                recordButtonClicked()
                buttonsVisible.value = true
                isRecording.value = true
            }
            true -> {
                pauseButtonClicked()
                isRecording.value = false
            }
        }
    }

    private fun recordButtonClicked() {
        when (resumeRecord) {
            true -> {
                timer.base = SystemClock.elapsedRealtime() - getStoppedTime()
                timer.start()
                resumeRecording()
            }
            false -> {
                resetTimer()
                timer.start()
                initializeRecorder()
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
        if (recorderInitialized) {
            showSnackBar(R.string.record_removed)
            endRecordSession()
        }
    }

    fun confirmRecord() {
        showSnackBar(R.string.record_saved)
        endRecordSession()
        saveRecordInDB()
    }

    private fun endRecordSession() {
        recorderInitialized = false
        buttonsVisible.value = false
        isRecording.value = false
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
        val recordingDuration = getRecordingDuration()
        if (recordingDuration != null) {
            val audio =
                EntryEntity(0, outputFile, getDate(), recordingDuration)
            dataSource.insert(audio)
        }
    }

    private fun getRecordingDuration(): String? {
        val metaRetriever = MediaMetadataRetriever()
        metaRetriever.setDataSource(outputFile)
        return DateUtils.formatElapsedTime(
            metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong() / (res.getInteger(
                R.integer.one_second
            ).toLong())
        )
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
    private fun getStoppedTime(): Int {
        val timeArray = currentRecordTime.split(":")
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
