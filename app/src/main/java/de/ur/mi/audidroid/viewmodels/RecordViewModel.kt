package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.media.MediaRecorder
import android.text.format.DateUtils
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import com.yashovardhan99.timeit.Stopwatch
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.models.RecordingEntity
import de.ur.mi.audidroid.models.MarkerEntity
import de.ur.mi.audidroid.models.LabelAssignmentEntity
import de.ur.mi.audidroid.models.MarkTimestamp
import de.ur.mi.audidroid.views.RecordFragment
import de.ur.mi.audidroid.utils.QuitRecordingDialog
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

/**
 * The ViewModel handles the changes to the view's data and the event logic for the user interaction referring to the [RecordFragment]
 * @author: Sabine Roth
 */

class RecordViewModel(
    private val dataSource: Repository,
    application: Application,
    private val activityContext: Context
) :
    AndroidViewModel(application), Stopwatch.OnTickListener {

    private val repository = dataSource
    private val mediaRecorder: MediaRecorder = MediaRecorder()
    private var tempFile = ""
    private var stopwatch: Stopwatch = Stopwatch()
    private lateinit var frameLayout: FrameLayout
    private var recorderInitialized = false
    private val context = getApplication<Application>().applicationContext
    val allMarkers: LiveData<List<MarkerEntity>> = repository.getAllMarkers()
    private var markList = ArrayList<ArrayList<String>>()
    var isRecording = MutableLiveData<Boolean>()
    var buttonsVisible = MutableLiveData<Boolean>()
    val res: Resources = context.resources
    private lateinit var stopwatchTextView: TextView
    private val _createDialog = MutableLiveData<Boolean>()
    var errorMessage: String? = null
    val createDialog: MutableLiveData<Boolean>
        get() = _createDialog

    init {
        isRecording.value = false
        buttonsVisible.value = false
        createDialog.value = false
    }

    fun initializeStopwatch(stopwatchTextView: TextView) {
        this.stopwatchTextView = stopwatchTextView
        stopwatch.clockDelay = (res.getInteger(
            R.integer.one_second
        )).toLong()
        stopwatch.setOnTickListener(this)
        stopwatchTextView.text = res.getString(R.string.start_time)
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
        when (stopwatch.isStarted) {
            false -> {
                recordButtonClicked()
                buttonsVisible.value = true
                isRecording.value = true
            }
            true -> {
                when (stopwatch.isPaused) {
                    false -> {
                        pauseButtonClicked()
                        isRecording.value = false
                    }
                    true -> {
                        recordButtonClicked()
                        isRecording.value = true
                    }
                }
            }
        }
    }

    private fun recordButtonClicked() {
        when (stopwatch.isPaused) {
            true -> {
                stopwatch.resume()
                resumeRecording()
            }
            false -> {
                stopwatch.start()
                initializeRecorder()
                startRecording()
            }
        }
    }

    private fun pauseButtonClicked() {
        pauseRecording()
        stopwatch.pause()
    }

    fun fragmentOnPause() {
        if (recorderInitialized && createDialog.value == false && stopwatchTextView.text != res.getString(
                R.string.start_time
            )
        ) deleteRecord()
    }

    fun onMarkerButtonClicked(markerEntity: MarkerEntity) {
        val elapsedTimeInMilli = stopwatch.elapsedTime
        val markEntry = ArrayList<String>()
        markEntry.add(markerEntity.uid.toString())
        markEntry.add(elapsedTimeInMilli.toString())
        markList.add(markEntry)
        Snackbar.make(frameLayout, R.string.mark_made, res.getInteger(R.integer.snackbar_quite_short)).show()
    }

    fun cancelRecord() {
        if (recorderInitialized && createDialog.value == false) {
            prepareForPossResume()
            QuitRecordingDialog.createDialog(
                activityContext,
                res.getString(R.string.quit_recording),
                this
            )
        }
    }

    fun deleteRecord() {
        showSnackBar(R.string.record_removed)
        File(tempFile).delete()
        endRecordSession()
        resetView()
    }

    fun cancelDialog() {
        errorMessage = null
        _createDialog.value = false
        buttonsVisible.value = true
        isRecording.value = false
    }

    fun confirmRecord() {
        prepareForPossResume()
        _createDialog.value = true
    }

    private fun prepareForPossResume() {
        if (isRecording.value!!) {
            mediaRecorder.pause()
            if (!stopwatch.isPaused) {
                stopwatch.pause()
            }
        }
    }

    private fun endRecordSession() {
        recorderInitialized = false
        mediaRecorder.stop()
        mediaRecorder.reset()
    }

    private fun resetView() {
        buttonsVisible.value = false
        isRecording.value = false
        resetStopwatch()
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
            errorDialog(res.getString(R.string.dialog_already_exist))
            return
        }
        endRecordSession()
        File(tempFile).copyTo(newFile)

        val recordingDuration = getRecordingDuration()
        val audio =
            RecordingEntity(
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

    private fun saveRecordInDB(audio: RecordingEntity, labels: ArrayList<Int>?) {
        val uid = dataSource.insertRecording(audio).toInt()
        if (labels != null) {
            for (i in labels.indices) {
                dataSource.insertRecLabels(LabelAssignmentEntity(0, uid, labels[i]))
            }
        }
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
        val duration = stopwatch.elapsedTime
        return DateUtils.formatElapsedTime(
            duration / (res.getInteger(
                R.integer.one_second
            )).toLong()
        )
    }

    private fun saveMarksInDB(recordingId: Int) {
        markList.forEach {
            val mark = MarkTimestamp(0, recordingId, it[0].toInt(), null, it[1].toInt())
            dataSource.insertMarkTimestamp(mark)
        }
        markList.clear()
    }

    private fun getDate(): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
    }

    private fun showSnackBar(text: Int) {
        Snackbar.make(frameLayout, text, Snackbar.LENGTH_LONG).show()
    }

    private fun resetStopwatch() {
        stopwatch.stop()
        stopwatchTextView.text = res.getString(R.string.start_time)
    }

    override fun onTick(stopwatch: Stopwatch?) {
        stopwatchTextView.text = DateUtils.formatElapsedTime(
            stopwatch!!.elapsedTime / (res.getInteger(
                R.integer.one_second
            )).toLong()
        )
    }
}
