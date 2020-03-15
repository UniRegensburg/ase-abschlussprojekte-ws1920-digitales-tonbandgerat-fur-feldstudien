package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.content.res.Resources
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.os.SystemClock
import android.text.format.DateUtils
import android.view.View
import android.widget.Chronometer
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import com.yashovardhan99.timeit.Stopwatch
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.LabelAssignmentEntity
import de.ur.mi.audidroid.models.MarkerTimeRelation
import de.ur.mi.audidroid.models.Repository
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

/**
 * The ViewModel handles the changes to the view's data and the event logic for the user interaction referring to the RecordFragment
 * @author: Sabine Roth
 */

class RecordViewModel(private val dataSource: Repository, application: Application) :
    AndroidViewModel(application) {

    private var resumeRecord = false
    private val mediaRecorder: MediaRecorder = MediaRecorder()
    private var tempFile = ""
    private var timerr: Stopwatch = Stopwatch()
    private lateinit var frameLayout: FrameLayout
    private var recorderInitialized = false
    private val context = getApplication<Application>().applicationContext
    private var markList = ArrayList<ArrayList<String>>()
    var isRecording = MutableLiveData<Boolean>()
    var buttonsVisible = MutableLiveData<Boolean>()
    val res: Resources = context.resources
    private val _createDialog = MutableLiveData<Boolean>()
    var errorMessage: String? = null
    lateinit var chronometer: TextView

    val createDialog: MutableLiveData<Boolean>
        get() = _createDialog

    init {
        buttonsVisible.value = false
        createDialog.value = false
    }

    fun initializeTimer(chronometer: TextView) {
        this.chronometer = chronometer
        timerr.clockDelay = 100
        timerr.setTextView(chronometer)
    }

    fun initializeLayout(frameLayout: FrameLayout) {
        this.frameLayout = frameLayout
    }

    /**Initializing the recorder and cache the recording in the internal memory till the user decides the save location in the dialog afterwards */
    private fun initializeRecorder() {
        tempFile =
            context.filesDir.absolutePath + res.getString(R.string.suffix_temp_file)
        with(mediaRecorder) {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(tempFile)
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
        when (timerr.isStarted) {
            false -> {
                recordButtonClicked()
                buttonsVisible.value = true
            }
            true -> {
                when (timerr.isPaused) {
                    false -> {
                        pauseButtonClicked()
                    }
                    true -> {
                        recordButtonClicked()
                    }
                }
            }
        }
    }

    private fun recordButtonClicked() {
        when (timerr.isPaused) {
            true -> {
                timerr.resume()
                resumeRecording()
            }
            false -> {
                //resetTimer()
                timerr.start()
                initializeRecorder()
                startRecording()
            }
        }
    }

    private fun pauseButtonClicked() {
        pauseRecording()
        timerr.pause()
    }

    fun cancelRecord() {
        if (recorderInitialized) {
            showSnackBar(R.string.record_removed)
            File(tempFile).delete()
            endRecordSession()
            resetView()
        }
    }

    fun cancelSaving() {
        errorMessage = null
        _createDialog.value = false
        buttonsVisible.value = true
        isRecording.value = false
        resumeRecord = true
    }

    fun confirmRecord() {
        prepareForPossResume()
        _createDialog.value = true
    }

    private fun prepareForPossResume() {
        mediaRecorder.pause()
        if (!timerr.isPaused) {
            timerr.pause()
        }
    }

    private fun endRecordSession() {
        recorderInitialized = false
        mediaRecorder.stop()
        mediaRecorder.reset()
    }

    private fun resetView() {
        buttonsVisible.value = false
        resumeRecord = false
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

    fun getNewFileFromUserInput(
        nameInput: String?,
        pathInput: String?,
        labels: ArrayList<Int>?
    ) {
        _createDialog.value = false
        val name = nameInput ?: java.lang.String.format(
            "%s_%s",
            res.getString(R.string.standard_name_recording),
            android.text.format.DateFormat.format(
                "yyyy-MM-dd_HH-mm",
                Calendar.getInstance(Locale.getDefault())
            )
        )
        if (!validNameInput(name)) {
            errorDialog(res.getString(R.string.dialog_invalid_name))
            return
        }
        if (name.length > res.getInteger(R.integer.max_name_length)) {
            errorDialog(res.getString(R.string.dialog_name_length))
            return
        }

        val path = java.lang.String.format(
            "%s/$name%s",
            (pathInput ?: context.filesDir.absolutePath),
            res.getString(R.string.suffix_audio_file)
        )
        val newFile = File(path)
        if (newFile.exists()) {
            errorMessage = res.getString(R.string.dialog_already_exist)
            _createDialog.value = true
            return
        }
        endRecordSession()
        File(tempFile).copyTo(newFile)

        val recordingDuration = getRecordingDuration()
        val audio =
            EntryEntity(
                uid = 0,
                recordingName = name,
                recordingPath = path,
                date = getDate(),
                duration = recordingDuration!!
            )
        saveRecordInDB(audio, labels)
        File(tempFile).delete()
        resetView()
        errorMessage = null
    }

    private fun saveRecordInDB(audio: EntryEntity, labels: ArrayList<Int>?) {
        val uid = dataSource.insertRecording(audio).toInt()
        if (labels != null) dataSource.insertRecLabels(LabelAssignmentEntity(0, uid, labels))
        if (markList.isNotEmpty()) {
            saveMarksInDB(uid)
        }
        showSnackBar(R.string.record_saved)
    }

    private fun validNameInput(name: String): Boolean {
        return Pattern.compile("^[a-zA-Z0-9_{}-]+$").matcher(name).matches()
    }

    private fun errorDialog(mes: String) {
        errorMessage = mes
        _createDialog.value = true
    }

    private fun getRecordingDuration(): String? {
        val time = timerr.elapsedTime
        return DateUtils.formatElapsedTime(time / 1000)
    }

    private fun saveMarksInDB(recordingId: Int) {
        markList.forEach {
            val mark = MarkerTimeRelation(0, recordingId, it[0], it[1], it[2])
            dataSource.insertMark(mark)
        }
        markList.clear()
    }

    fun makeMark(view: View) {
        val btnId = view.resources.getResourceName(view.id)
        val elapsedTimeInMilli = timerr.elapsedTime
        val elapsedTimeInSec = DateUtils.formatElapsedTime(timerr.elapsedTime / 1000)
        val markEntry = ArrayList<String>()
        markEntry.add(btnId)
        markEntry.add(elapsedTimeInSec)
        markEntry.add(elapsedTimeInMilli.toString())
        markList.add(markEntry)
        showSnackBarShort(R.string.mark_made)
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

    private fun showSnackBarShort(text: Int) {
        Snackbar.make(frameLayout, text, res.getInteger(R.integer.snackbar_quite_short)).show()
    }

    /** Resets timer to 00:00 */
    private fun resetTimer() {
        timerr.stop()
    }
}
