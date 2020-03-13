package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.utils.AudioEditor
import de.ur.mi.audidroid.utils.FFMpegCallback
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.*
import io.apptik.widget.MultiSlider
import io.apptik.widget.MultiSlider.SimpleChangeListener
import io.apptik.widget.MultiSlider.Thumb
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class EditRecordingViewModel(
    private val recordingId: Int,
    dataSource: Repository,
    application: Application
) :
    AndroidViewModel(application) {

    private val repository = dataSource
    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private lateinit var frameLayout: FrameLayout
    private lateinit var seekBar: SeekBar
    private lateinit var rangeBar: MultiSlider
    private val context = getApplication<Application>().applicationContext
    private val res = context.resources
    val recording: LiveData<EntryEntity> =
        repository.getRecordingById(recordingId)
    val allMarks: LiveData<List<MarkerTimeRelation>> = repository.getAllMarks(recordingId)
    private val oneSecond: Long = res.getInteger(R.integer.one_second).toLong()
    var isPlaying = MutableLiveData<Boolean>()
    var audioInProgress = MutableLiveData<Boolean>()
    var enableCutInner = MutableLiveData<Boolean>()
    var enableCutOuter = MutableLiveData<Boolean>()
    var isPlayerViewModel = MutableLiveData<Boolean>()
    var tempFile = ""
    var errorMessage: String? = null
    private var editRecordingId: Int = 0

    private lateinit var runnable: Runnable
    private var handler: Handler = Handler()

    private val _createDialog = MutableLiveData<Boolean>()
    val createDialog: LiveData<Boolean>
        get() = _createDialog

    private val _totalDuration = MutableLiveData<Long>()
    private val totalDuration: LiveData<Long>
        get() = _totalDuration

    var totalDurationString = Transformations.map(totalDuration) { duration ->
        DateUtils.formatElapsedTime(duration)
    }

    private val _currentDuration = MutableLiveData<Long>()
    private val currentDuration: LiveData<Long>
        get() = _currentDuration

    // The String version of the current duration
    val currentDurationString = Transformations.map(currentDuration) { duration ->
        DateUtils.formatElapsedTime(duration)
    }

    private val _curPosThumb1 = MutableLiveData<Long>()
    private val curPosThumb1: LiveData<Long>
        get() = _curPosThumb1

    val curPosThumb1String = Transformations.map(curPosThumb1) { posThumb1 ->
        DateUtils.formatElapsedTime(posThumb1)
    }

    private val _curPosThumb2 = MutableLiveData<Long>()
    private val curPosThumb2: LiveData<Long>
        get() = _curPosThumb2

    val curPosThumb2String = Transformations.map(curPosThumb2) { posThumb2 ->
        DateUtils.formatElapsedTime(posThumb2)
    }

    // If there are no recordings in the database, a TextView is displayed.
    val empty: LiveData<Boolean> = Transformations.map(allMarks) {
        it.isEmpty()
    }

    fun initializeMediaPlayer() {
        isPlaying.value = false
        isPlayerViewModel.value = false
        val uri: Uri = Uri.fromFile(File(tempFile))
        mediaPlayer = MediaPlayer().apply {
            try {
                reset()
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(context, uri)
                setOnCompletionListener {
                    onStopPlayer()
                }
                prepare()
            } catch (e: IOException) {
                showSnackBar(R.string.error_message_initialization_failed)
            } catch (e: IllegalArgumentException) {
                showSnackBar(R.string.error_message_path)
            }
        }
    }

    fun initializeSeekBar(seekBar: SeekBar) {
        this.seekBar = seekBar
        seekBar.max = mediaPlayer.duration
        _currentDuration.value =
            mediaPlayer.currentPosition / oneSecond
        _totalDuration.value = mediaPlayer.duration / oneSecond
        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            }
        )

        runnable = Runnable {
            seekBar.progress = mediaPlayer.currentPosition
            _currentDuration.value =
                mediaPlayer.currentPosition / oneSecond
            handler.postDelayed(runnable, 50)
        }

        handler.postDelayed(runnable, 0)
    }

    fun initializeFrameLayout(frameLayout: FrameLayout) {
        this.frameLayout = frameLayout
    }

    fun onStartPlayer() {
        mediaPlayer.start()
        handler.postDelayed(runnable, 0)
        isPlaying.value = mediaPlayer.isPlaying
    }

    fun onPausePlayer() {
        mediaPlayer.pause()
        _currentDuration.value =
            mediaPlayer.currentPosition / oneSecond
        isPlaying.value = mediaPlayer.isPlaying
    }

    private fun onStopPlayer() {
        mediaPlayer.stop()
        handler.removeCallbacks(runnable)
        isPlaying.value = mediaPlayer.isPlaying
        initializeMediaPlayer()
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(runnable)
        mediaPlayer.reset()
        mediaPlayer.release()
    }

    private fun showSnackBar(text: Int) {
        Snackbar.make(frameLayout, text, Snackbar.LENGTH_LONG).show()
    }

    fun initializeRangeBar(rangeBar: MultiSlider) {
        this.rangeBar = rangeBar
        audioInProgress.value = false
        enableCutInner.value = true
        enableCutOuter.value = false
        rangeBar.max = mediaPlayer.duration
        configureThumb1(rangeBar)
        configureThumb2(rangeBar)

        rangeBar.setOnThumbValueChangeListener(object : SimpleChangeListener() {
            override fun onValueChanged(
                multiSlider: MultiSlider?,
                thumb: Thumb?,
                thumbIndex: Int,
                value: Int
            ) {

                if (thumbIndex == 0) {
                    _curPosThumb1.value = value / oneSecond
                } else {
                    _curPosThumb2.value = value / oneSecond
                }

                enableButtons()

            }
        })
    }

    private fun configureThumb1(rangeBar: MultiSlider) {
        val thumb1 = rangeBar.getThumb(0)
        thumb1.value = 0
        _curPosThumb1.value = thumb1.value / oneSecond
    }

    private fun configureThumb2(rangeBar: MultiSlider) {
        val thumb2 = rangeBar.getThumb(1)
        thumb2.value = mediaPlayer.duration
        _curPosThumb2.value = thumb2.value / oneSecond
    }

    private fun enableButtons() {
        if (_curPosThumb1.value!! != _curPosThumb2.value!!) {
            enableCutInner.value = true
            enableCutOuter.value = true

            if ((_curPosThumb1.value!! != 0.toLong()) && (_curPosThumb2.value!! != mediaPlayer.duration / oneSecond)) {
                enableCutInner.value = true
                enableCutOuter.value = true
            } else {
                enableCutInner.value = true
                enableCutOuter.value = false
            }
        } else {
            enableCutInner.value = false
            enableCutOuter.value = false
        }
    }

    val callback = object : FFMpegCallback {
        override fun onSuccess(convertedFile: File, startTime: String, endTime: String) {
            audioInProgress.value = false
            tempFile = convertedFile.path
            initializeMediaPlayer()
            initializeSeekBar(seekBar)
            initializeRangeBar(rangeBar)
            initializeFrameLayout(frameLayout)
            showSnackBar(R.string.recording_cut)

            saveEditRecordingInDb(convertedFile, startTime, endTime)
            //saveinDatenbank getId
        }

        override fun onFailure(error: Exception) {
            audioInProgress.value = false
            showSnackBar(R.string.error_message_cut)
        }
    }

    private fun saveEditRecordingInDb(convertedFile: File, startTime: String, endTime: String) {

        val recordingDuration = getRecordingDuration(convertedFile)
        val audio =
            EntryEntity(
                uid = 0,
                recordingName = convertedFile.name,
                recordingPath = convertedFile.path,
                date = getDate(),
                duration = recordingDuration!!
            )
        saveRecordInDB(audio, startTime, endTime)
    }

    private fun saveRecordInDB(audio: EntryEntity, startTime: String, endTime: String) {
        editRecordingId = repository.insertRecording(audio).toInt()
        Log.d("recordingId", "" + allMarks.value)
        /*if (markList.isNotEmpty()) {
            saveMarksInDB(uid)
        }*/
    }


    private fun getOutputPath(): String {
        return context.filesDir.absolutePath
    }

    fun cutInner() {
        audioInProgress.value = true
        val editor = AudioEditor()
        with(editor) {
            setFile(File(tempFile))
            setStartTime(curPosThumb1String.value!!)
            setEndTime(curPosThumb2String.value!!)
            setOutputPath(getOutputPath())
            setOutputFileName("trimmed_" + System.currentTimeMillis() + ".aac")
            setCallback(callback)
            cut("cutInner")
        }
    }

    fun cutOuter() {
        audioInProgress.value = true
        val duration = mediaPlayer.duration / oneSecond
        val editor = AudioEditor()
        with(editor) {
            setFile(File(tempFile))
            setStartTime(curPosThumb1.value.toString())
            setEndTime(curPosThumb2.value.toString())
            setDuration(duration.toString())
            setOutputPath(getOutputPath())
            setOutputFileName("trimmed_" + System.currentTimeMillis() + ".aac")
            setCallback(callback)
            cut("cutOuter")
        }
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

        File(tempFile).copyTo(newFile)

        val recordingDuration = getRecordingDuration(File(tempFile))
        val audio =
            EntryEntity(
                uid = 0,
                recordingName = name,
                recordingPath = path,
                date = getDate(),
                duration = recordingDuration!!
            )
        updateRecordingInDB(audio, labels)
        errorMessage = null
    }

    private fun updateRecordingInDB(audio: EntryEntity, labels: ArrayList<Int>?) {
        repository.updateRecording(audio)
        if (labels != null) repository.insertRecLabels(
            LabelAssignmentEntity(
                0,
                editRecordingId,
                labels
            )
        )
        showSnackBar(R.string.record_saved)
    }

    fun saveRecording() {
        _createDialog.value = true
    }

    fun cancelSaving() {
        errorMessage = null
        _createDialog.value = false
    }

    private fun validNameInput(name: String): Boolean {
        return Pattern.compile("^[a-zA-Z0-9_{}-]+$").matcher(name).matches()
    }

    private fun errorDialog(mes: String) {
        errorMessage = mes
        _createDialog.value = true
    }

    fun onMarkClicked(markTime: String) {
    }

    fun addMark(view: View) {
        val btnId = view.resources.getResourceName(view.id)
        val mark = MarkerTimeRelation(
            0,
            recordingId,
            btnId,
            currentDurationString.value!!,
            _currentDuration.value.toString()
        )
        repository.insertMark(mark)
        showSnackBar(R.string.mark_made)
    }

    fun deleteMark(mid: Int) {
        repository.deleteMark(mid)
        showSnackBar(R.string.mark_deleted)
    }

    private fun getRecordingDuration(file: File): String? {
        val uri: Uri = Uri.fromFile(file)
        val metaRetriever = MediaMetadataRetriever()
        metaRetriever.setDataSource(context, uri)
        return DateUtils.formatElapsedTime(
            metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                .toLong() / (res.getInteger(
                R.integer.one_second
            ).toLong())
        )
    }

    private fun getDate(): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
    }
}
