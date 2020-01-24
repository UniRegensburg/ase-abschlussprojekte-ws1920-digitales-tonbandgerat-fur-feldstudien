package de.ur.mi.audidroid.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_SPEECH
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.text.format.DateUtils
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.Repository
import java.io.File
import java.io.IOException
import java.lang.IllegalArgumentException

/**
 * ViewModel for PlayerFragment.
 * @author: Theresa Strohmeier
 */
class PlayerViewModel(
    recordingPath: String,
    dataSource: Repository,
    application: Application
) : AndroidViewModel(application) {

    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private val context = getApplication<Application>().applicationContext
    private val res = context.resources
    private val uri: Uri = Uri.fromFile(File(recordingPath))
    var isPlaying = MutableLiveData<Boolean>()

    private lateinit var runnable: Runnable
    private var handler: Handler = Handler()

    var totalDurationString = ""

    private val _currentDuration = MutableLiveData<Long>()
    private val currentDuration: LiveData<Long>
        get() = _currentDuration

    // The String version of the current duration
    val currentDurationString = Transformations.map(currentDuration) { duration ->
        DateUtils.formatElapsedTime(duration)
    }

    fun initializeMediaPlayer() {
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
                e.printStackTrace()
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
    }

    fun initializeSeekBar(seekBar: SeekBar) {
        seekBar.max = mediaPlayer.duration
        _currentDuration.value =
            mediaPlayer.currentPosition / res.getInteger(R.integer.one_second).toLong()
        totalDurationString =
            DateUtils.formatElapsedTime(mediaPlayer.duration / res.getInteger(R.integer.one_second).toLong())

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
                mediaPlayer.currentPosition / res.getInteger(R.integer.one_second).toLong()
            handler.postDelayed(runnable, 50)
        }

        handler.postDelayed(runnable, 0)
    }

    fun onStartPlayer() {
        mediaPlayer.start()
        handler.postDelayed(runnable, 0)
        isPlaying.value = mediaPlayer.isPlaying
    }

    fun onPausePlayer() {
        mediaPlayer.pause()
        _currentDuration.value =
            mediaPlayer.currentPosition / res.getInteger(R.integer.one_second).toLong()
        isPlaying.value = mediaPlayer.isPlaying
    }

    fun onStopPlayer() {
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
}