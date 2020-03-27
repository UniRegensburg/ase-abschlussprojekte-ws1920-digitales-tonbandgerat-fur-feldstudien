package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.content.ContentResolver
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.text.format.DateUtils
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.arthenica.mobileffmpeg.Config.getPackageName
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.LabelAssignmentEntity
import de.ur.mi.audidroid.models.MarkAndTimestamp
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.AudioEditor
import de.ur.mi.audidroid.utils.FFMpegCallback
import de.ur.mi.audidroid.utils.HandlePlayerBar
import de.ur.mi.audidroid.utils.SoundBar
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
    application: Application,
    val handlePlayerBar: HandlePlayerBar
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
    val allMarks: LiveData<List<MarkAndTimestamp>> = repository.getAllMarks(recordingId)
    private val oneSecond: Long = res.getInteger(R.integer.one_second).toLong()
    var isPlaying = MutableLiveData<Boolean>()
    var audioInProgress = MutableLiveData<Boolean>()
    var enableCutInner = MutableLiveData<Boolean>()
    var enableCutOuter = MutableLiveData<Boolean>()
    var tempFile = ""
    var errorMessage: String? = null
    private lateinit var soundBar: SoundBar


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
                    soundBar.updatePlayerPercent(mediaPlayer.currentPosition / mediaPlayer.duration.toFloat())
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

    fun initializeVisualizer(bar: SoundBar) {
        soundBar = bar
        val bytes = File(tempFile).readBytes()

        val percentage = mediaPlayer.currentPosition / mediaPlayer.duration.toFloat()



        val temp : Uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + File.pathSeparator + File.separator + "de.ur.mi.audidroid/raw/" + "audio_sample.mp3")

        val tmep1 : Uri = Uri.parse("android.resource://de.ur.mi.audidroid/" + R.raw.audio_sample)
        val raw = "android.resource://de.ur.mi.audidroid/raw/audio_sample"

        val uri: Uri = Uri.parse(raw)

        //val file = File(temp)
        //val tempBytes = file.readBytes()

        val urii = Uri.fromFile(File(tempFile))
        soundBar.updateVisualizer(urii)
        //soundBar.updateVisualizer(bytes)
        soundBar.updatePlayerPercent(percentage)


        //val playerVisualizerView = PlayerVisualizerView(context)


        /*   val bufferData = DoubleArray(bytes.size)
           val bytesPerSample : Int = 2
           val amplification : Double= 100.0
           for(index in 0..bytes.size){
               var sample : Double = 0.0
               for(b in 0..bytesPerSample){
                   var v : Int = bufferData[index].toInt()
                   if(b<bytesPerSample -1 || bytesPerSample == 1){
                       v = v and 0xFF
                   }
                   sample += (v shl (b*8))
               }
               val sample32 = amplification * (sample / 32768.0)
               bufferData[index] = sample32
           }
   */
/*
        val visualizer= Visualizer(mediaPlayer.audioSessionId)

        visualizer.enabled = true

        val boolean: Boolean = visualizer.enabled

        val success = visualizer.getWaveForm(bytes)

        if(success==0){

        }*/

        /* if (audioId != -1) {
             visualizer.setAudioSessionId(audioId)
         }*/
        /*  val audioAttributes = AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_MEDIA)
              .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
              .build()
          val audioFormat = AudioFormat.Builder()
              .setEncoding(AudioFormat.ENCODING_AAC_XHE)
              .setSampleRate(1000)
              .build()

          val track = AudioTrack.Builder()
              .setAudioAttributes(audioAttributes)
              .setAudioFormat(audioFormat)
              .setBufferSizeInBytes(1000)
              .setTransferMode(AudioTrack.MODE_STATIC)
              .build()*/
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
        initializeVisualizer(soundBar)
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
            initializeFrameLayout(frameLayout)
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
        saveRecordInDB(audio, labels)
        File(tempFile).delete()
        errorMessage = null
    }

    private fun saveRecordInDB(audio: EntryEntity, labels: ArrayList<Int>?) {
        val uid = repository.insertRecording(audio).toInt()
        if (labels != null) repository.insertRecLabels(LabelAssignmentEntity(0, uid, labels))
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
//        val btnId = view.resources.getResourceName(view.id)
//        val mark = MarkerTimeRelation(0, recordingId, btnId, currentDurationString.value!!)
//        repository.insertMark(mark)
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

    fun skipPlaying() {
        handlePlayerBar.doSkippingPlaying(mediaPlayer, context)
    }

    fun returnPlaying() {
        handlePlayerBar.doReturnPlaying(mediaPlayer, context)
    }
}
