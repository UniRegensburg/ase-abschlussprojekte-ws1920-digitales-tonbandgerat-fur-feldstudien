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

    /*fun delete(entryEntity: EntryEntity) {
        repository.delete(entryEntity)
    }*/

    fun onButtonClicked(entryEntity: EntryEntity, view: View) {
        val popupMenu: PopupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete_recording ->
                    repository.delete(entryEntity)
                R.id.action_edit_recording ->
                    Log.d("clicked", "edit")
            }
            true
        }
        popupMenu.show()
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
