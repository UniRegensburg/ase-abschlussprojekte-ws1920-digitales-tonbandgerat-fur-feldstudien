package de.ur.mi.audidroid.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.Repository

/**
 * ViewModel for FilesFragment.
 * @author
 */
class FilesViewModel (dataSource: Repository, application: Application) : AndroidViewModel(application) {

    private val repository = dataSource

    val allRecordings: LiveData<List<EntryEntity>> = repository.getAllRecordings()

    fun delete(entryEntity: EntryEntity) {
        repository.delete(entryEntity)
    }

    // Navigation to PlayerFragment
    private val _navigateToPlayerFragment = MutableLiveData<String>()
    val navigateToPlayerFragment
        get() = _navigateToPlayerFragment

    fun onRecordingClicked(recordingPath: String){
        _navigateToPlayerFragment.value = recordingPath
    }

    fun onPlayerFragmentNavigated() {
        _navigateToPlayerFragment.value = null
    }
}
