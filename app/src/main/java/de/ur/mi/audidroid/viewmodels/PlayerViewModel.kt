package de.ur.mi.audidroid.viewmodels

import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import de.ur.mi.audidroid.models.Repository
import java.io.IOException

/**
 * ViewModel for PlayerFragment.
 * @author
 */
class PlayerViewModel(private val recordingPath: String, dataSource: Repository) : ViewModel() {

    private val repository = dataSource

    //private var viewModelJob = Job()
    //private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    fun onStartPlayer() {
    }

    fun onPausePlayer() {
        Log.d("button", "onPausePlay")
    }

    /*override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }*/
}