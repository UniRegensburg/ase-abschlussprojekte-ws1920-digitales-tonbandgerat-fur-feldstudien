package de.ur.mi.audidroid.viewmodels

import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import de.ur.mi.audidroid.models.EntryRepository
import java.io.IOException

class ReplayViewModel (private val recordingPath: String, dataSource: EntryRepository) : ViewModel(){

    private lateinit var mediaPlayer: MediaPlayer
    private val repository = dataSource

    //private var viewModelJob = Job()
    //private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    fun onStartReplay() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(recordingPath)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e("output", "prepare() failed")
            }
        }
    }

    fun onPauseReplay(){
        Log.d("button","onPausePlay")
    }

    /*override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }*/
}