package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.Repository

/**
 * ViewModel for FilesFragment.
 * @author
 */
class FilesViewModel(dataSource: Repository, application: Application) :
    AndroidViewModel(application) {

    private val repository = dataSource
    private val context = getApplication<Application>().applicationContext
    val allRecordings: LiveData<List<EntryEntity>> = repository.getAllRecordings()

    private var _showSnackbarEvent = MutableLiveData<Boolean?>()

    val showSnackbarEvent: LiveData<Boolean?>
        get() = _showSnackbarEvent

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = null
    }

    fun onButtonClicked(entryEntity: EntryEntity, view: View) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete_recording ->
                    delete(entryEntity)
            }
            true
        }
        popupMenu.show()
    }

    fun delete(entryEntity: EntryEntity) {
        repository.delete(entryEntity)
        _showSnackbarEvent.value = true
    }

    // Navigation to PlayerFragment
    private val _navigateToPlayerFragment = MutableLiveData<String>()
    val navigateToPlayerFragment
        get() = _navigateToPlayerFragment

    fun onRecordingClicked(recordingPath: String) {
        _navigateToPlayerFragment.value = recordingPath
    }

    fun onPlayerFragmentNavigated() {
        _navigateToPlayerFragment.value = null
    }
}
