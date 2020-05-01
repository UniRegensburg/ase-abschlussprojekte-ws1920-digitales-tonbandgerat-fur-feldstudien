package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_SPEECH
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.text.format.DateUtils
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.*
import de.ur.mi.audidroid.utils.HandlePlayerBar
import java.io.File
import java.io.IOException


/**
 * ViewModel for PlayerFragment.
 * @author: Theresa Strohmeier
 */
class PlayerViewModel(
    recordingId: Int,
    dataSource: Repository,
    application: Application
) : AndroidViewModel(application) {

    private val repository = dataSource
    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private lateinit var frameLayout: FrameLayout
    private lateinit var buttonFastForward: ImageButton
    private lateinit var buttonFastRewind: ImageButton
    private val context = getApplication<Application>().applicationContext
    private val res = context.resources
    private val oneSecond: Long = res.getInteger(R.integer.one_second).toLong()
    val recording: LiveData<EntryEntity> =
        repository.getRecordingById(recordingId)
    val allMarks: LiveData<List<MarkAndTimestamp>> = repository.getAllMarks(recordingId)
    val allLabels: LiveData<List<LabelEntity>> = repository.getRecLabelsById(recordingId)
    var isPlaying = MutableLiveData<Boolean>()
    var recordingPath = ""
    private lateinit var seekBar: SeekBar

    private lateinit var runnable: Runnable
    private var handler: Handler = Handler()

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

    // If there are no marks in the database, a TextView is displayed.
    val noMarks: LiveData<Boolean> = Transformations.map(allMarks) {
        it.isEmpty()
    }

    val noLabels: LiveData<Boolean> = Transformations.map(allLabels) {
        it.isEmpty()
    }

    fun initializeMediaPlayer() {
        isPlaying.value = false
        val uri: Uri = Uri.fromFile(File(recordingPath))
        mediaPlayer = MediaPlayer().apply {
            try {
                reset()
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(CONTENT_TYPE_SPEECH)
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
        buttonFastForward = frameLayout.findViewById(R.id.bar_fast_forward)
        buttonFastRewind = frameLayout.findViewById(R.id.bar_fast_rewind)
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
        mediaPlayer.stop()
    }

    fun fragmentOnPause() {
        mediaPlayer.stop()
    }

    private fun showSnackBar(text: Int) {
        Snackbar.make(frameLayout, text, Snackbar.LENGTH_LONG).show()
    }

    fun onMarkTimeClicked(markTime: Int) {
        mediaPlayer.seekTo(markTime)
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
        HandlePlayerBar.fastRewind(mediaPlayer, context, buttonFastRewind, buttonFastForward)
    }

    private fun resetPlayerBar(){
        buttonFastRewind.backgroundTintList = ContextCompat.getColorStateList(context, R.color.color_on_surface)
        buttonFastForward.backgroundTintList = ContextCompat.getColorStateList(context, R.color.color_on_surface)
    }
}
