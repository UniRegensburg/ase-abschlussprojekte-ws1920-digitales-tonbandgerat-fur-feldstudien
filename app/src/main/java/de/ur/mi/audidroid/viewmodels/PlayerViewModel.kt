package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_SPEECH
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.ur.mi.audidroid.databinding.PlayerFragmentBinding
import de.ur.mi.audidroid.models.Repository
import java.io.File
import java.io.IOException
import java.lang.IllegalArgumentException

/**
 * ViewModel for PlayerFragment.
 * @author
 */
class PlayerViewModel(
    recordingPath: String,
    dataSource: Repository,
    application: Application
) : AndroidViewModel(application) {

    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private val context = getApplication<Application>().applicationContext
    private val uri: Uri = Uri.fromFile(File(recordingPath))

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
                prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
    }

    fun onStartPlayer() {
        mediaPlayer.start()
        Log.d("mediaplayer", "play")
    }

    fun onPausePlayer() {
        mediaPlayer.pause()
        Log.d("mediaplayer", "pause")
    }
}