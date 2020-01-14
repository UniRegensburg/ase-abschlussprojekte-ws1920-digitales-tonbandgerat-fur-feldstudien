package de.ur.mi.audidroid.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.EntryRepository

class FilesViewModel (dataSource: EntryRepository, application: Application) : AndroidViewModel(application) {

    private val repository = dataSource

    val allRecordings: LiveData<List<EntryEntity>> = repository.getAllRecordings()

    fun delete(entryEntity: EntryEntity) {
        repository.delete(entryEntity)
    }

    // Navigation to ReplayFragment
    private val _navigateToReplayFragment = MutableLiveData<String>()
    val navigateToReplayFragment
        get() = _navigateToReplayFragment

    fun onRecordingClicked(recordingPath: String){
        _navigateToReplayFragment.value = recordingPath
    }

    fun onReplayFragmentNavigated() {
        _navigateToReplayFragment.value = null
    }
}
