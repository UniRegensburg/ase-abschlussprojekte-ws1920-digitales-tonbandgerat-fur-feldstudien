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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.LabelAssignmentEntity
import de.ur.mi.audidroid.models.MarkAndTimestamp
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.models.ExpandableMarkAndTimestamp
import de.ur.mi.audidroid.models.MarkTimestamp
import de.ur.mi.audidroid.utils.AudioEditor
import de.ur.mi.audidroid.utils.FFMpegCallback
import de.ur.mi.audidroid.utils.HandlePlayerBar
import io.apptik.widget.MultiSlider
import io.apptik.widget.MultiSlider.SimpleChangeListener
import io.apptik.widget.MultiSlider.Thumb
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class EditRecordingViewModel(
    recordingId: Int,
    dataSource: Repository,
    application: Application
) :
    AndroidViewModel(application) {

    private val repository = dataSource
    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private lateinit var frameLayout: FrameLayout
    private lateinit var buttonFastForward: ImageButton
    private lateinit var buttonFastRewind: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var rangeBar: MultiSlider
    private val context = getApplication<Application>().applicationContext
    private val res = context.resources
    val recording: LiveData<EntryEntity> = repository.getRecordingById(recordingId)
    val allMarks: LiveData<List<MarkAndTimestamp>> = repository.getAllMarks(recordingId)
    private val oneSecond: Long = res.getInteger(R.integer.one_second).toLong()
    var isPlaying = MutableLiveData<Boolean>()
    var audioInProgress = MutableLiveData<Boolean>()
    var enableCutInner = MutableLiveData<Boolean>()
    var enableCutOuter = MutableLiveData<Boolean>()
    var tempFile = ""
    var saveErrorMessage: String? = null
    var commentErrorMessage: String? = null
    var markTimestampToBeEdited: ExpandableMarkAndTimestamp? = null
    var markToBeDeleted: MarkAndTimestamp? = null


    private lateinit var runnable: Runnable
    private var handler: Handler = Handler()

    private val _createSaveDialog = MutableLiveData<Boolean>()
    val createSaveDialog: LiveData<Boolean>
        get() = _createSaveDialog

    private val _createCommentDialog = MutableLiveData<Boolean>()
    val createCommentDialog: LiveData<Boolean>
        get() = _createCommentDialog

    private val _createConfirmDialog = MutableLiveData<Boolean>()
    val createConfirmDialog: MutableLiveData<Boolean>
        get() = _createConfirmDialog

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

    fun initializeLayout(frameLayout: FrameLayout) {
        this.frameLayout = frameLayout
        buttonFastForward = frameLayout.findViewById(R.id.bar_fast_forward)
        buttonFastRewind = frameLayout.findViewById(R.id.bar_fast_rewind)
        initializeVisualizer()
    }


    /**
     * Visualizing the sound track of the recording through creating an image of the audio waves using FFMPEG
     * @author: Sabine Roth
     */

    private fun initializeVisualizer(size: String = "640x120") {
        val internalAudioCopy = File(context.filesDir, "internalCopy")
        File(tempFile).copyTo(internalAudioCopy)

        val wavePic = File(context.filesDir, "waveform.png")
        val colorHex = "#" + Integer.toHexString(
            ContextCompat.getColor(
                context,
                R.color.color_primary
            ) and 0x00ffffff
        )
        val command =
            "-i ${internalAudioCopy.path} -filter_complex \"compand=attacks=0:points=25/35:gain=4,showwavespic=s=$size:colors=$colorHex\" -frames:v 1 ${wavePic.path}"

        try {
            when (FFmpeg.execute(command)) {
                Config.RETURN_CODE_SUCCESS -> {
                    if (wavePic.exists()) {
                        val image = frameLayout.findViewById<ImageView>(R.id.waveView)
                        image.setImageURI(null)
                        image.setImageURI(Uri.fromFile(wavePic))
                        wavePic.delete()
                        internalAudioCopy.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WavePic", "preparation failed")
            internalAudioCopy.delete()
            if (wavePic.exists()) wavePic.delete()
        }
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
        initializeSeekBar(seekBar)
        resetPlayerBar()
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
        override fun onSuccess(convertedFile: File) {
            audioInProgress.value = false
            tempFile = convertedFile.path
            initializeMediaPlayer()
            initializeSeekBar(seekBar)
            initializeRangeBar(rangeBar)
            initializeLayout(frameLayout)
            showSnackBar(R.string.recording_cut)
        }

        override fun onFailure(error: Exception) {
            audioInProgress.value = false
            showSnackBar(R.string.error_message_cut)
        }
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
        _createSaveDialog.value = false
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
            saveErrorMessage = res.getString(R.string.dialog_already_exist)
            _createSaveDialog.value = true
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
        saveRecordInDB(audio, labels)
        File(tempFile).delete()
        saveErrorMessage = null
    }

    private fun saveRecordInDB(audio: EntryEntity, labels: ArrayList<Int>?) {
        val uid = repository.insertRecording(audio).toInt()
        if (labels != null) {
            for (i in labels.indices) {
                repository.insertRecLabels(LabelAssignmentEntity(0, uid, labels[i]))
            }
        }
        showSnackBar(R.string.record_saved)
    }

    fun saveRecording() {
        _createSaveDialog.value = true
    }

    fun cancelSaving() {
        saveErrorMessage = null
        _createSaveDialog.value = false
    }

    fun onEditCommentClicked(mark: ExpandableMarkAndTimestamp) {
        markTimestampToBeEdited = mark
        _createCommentDialog.value = true
    }

    fun onMarkTimestampUpdateClicked(newComment: String?, mark: ExpandableMarkAndTimestamp) {
        _createCommentDialog.value = false
        mark.isExpanded = false
        updateMarkAndTimestampInDB(newComment, mark.markAndTimestamp.markTimestamp)
    }

    fun cancelCommentSaving() {
        commentErrorMessage = null
        _createCommentDialog.value = false
    }

    private fun validNameInput(name: String): Boolean {
        return Pattern.compile("^[a-zA-Z0-9_{}-]+$").matcher(name).matches()
    }

    private fun errorDialog(mes: String) {
        saveErrorMessage = mes
        _createSaveDialog.value = true
    }

    fun addMark(view: View) {
//        val btnId = view.resources.getResourceName(view.id)
//        val mark = MarkerTimeRelation(0, recordingId, btnId, currentDurationString.value!!)
//        repository.insertMark(mark)
        showSnackBar(R.string.mark_made)
    }

    private fun updateMarkAndTimestampInDB(newComment: String?, markTimestamp: MarkTimestamp) {
        val updatedMarkTimestamp = MarkTimestamp(
            markTimestamp.mid,
            markTimestamp.recordingId,
            markTimestamp.markerId,
            newComment,
            markTimestamp.markTime
        )
        repository.updateMarkTimestamp(updatedMarkTimestamp)
        markTimestampToBeEdited = null
        showSnackBar(R.string.comment_updated)
    }

    fun onMarkDeleteClicked(markAndTimestamp: MarkAndTimestamp) {
        markToBeDeleted = markAndTimestamp
        _createConfirmDialog.value = true
    }

    fun deleteMark(mid: Int) {
        repository.deleteMark(mid)
        _createConfirmDialog.value = false
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

    fun skipPlaying() {
        HandlePlayerBar.skipPlaying(mediaPlayer, context)
    }

    fun returnPlaying() {
        HandlePlayerBar.returnPlaying(mediaPlayer, context)
    }

    fun fastForward(){
        HandlePlayerBar.fastForward(mediaPlayer, context, buttonFastForward, buttonFastRewind)
    }

    fun fastRewind(){
        HandlePlayerBar.fastRewind(mediaPlayer, context, buttonFastRewind, buttonFastRewind)
    }

    private fun resetPlayerBar(){
        buttonFastRewind.backgroundTintList = ContextCompat.getColorStateList(context, R.color.color_on_surface)
        buttonFastForward.backgroundTintList = ContextCompat.getColorStateList(context, R.color.color_on_surface)
    }

    fun cancelDelete() {
        _createConfirmDialog.value = false
    }
}
