package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.view.View
import android.widget.PopupMenu
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.ShareHelper
import de.ur.mi.audidroid.views.MainActivity
import java.io.File

/**
 * ViewModel for FilesFragment.
 * @author: Theresa Strohmeier
 */
class FilesViewModel(dataSource: Repository, application: Application) :
    AndroidViewModel(application) {

    private val repository = dataSource
    private val context = getApplication<Application>().applicationContext
    private val _createAlertDialog = MutableLiveData<Boolean>()
    val allRecordings: LiveData<List<EntryEntity>> = repository.getAllRecordings()
    private var recordingToBeExported: EntryEntity? = null

    private var _showSnackbarEvent = MutableLiveData<Boolean>()

    val createAlertDialog: MutableLiveData<Boolean>
        get() = _createAlertDialog

    val showSnackbarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = null
    }

    // If there are no recordings in the database, a TextView is displayed.
    val empty: LiveData<Boolean> = Transformations.map(allRecordings) {
        it.isEmpty()
    }

    // When the ImageButton is clicked, a PopupMenu opens.
    fun onButtonClicked(entryEntity: EntryEntity, view: View) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete_recording ->
                    delete(entryEntity)
                R.id.action_share_recording -> {
                    recordingToBeExported = entryEntity
                    _createAlertDialog.value = true
                }
            }
            true
        }
        popupMenu.show()
    }

    fun shareRecording(format: String) {
        ShareHelper.shareAudio(recordingToBeExported!!, format, context)
    }

    private fun delete(entryEntity: EntryEntity) {
        val deletedSuccessful = File(entryEntity.recordingPath).delete()
        if (deletedSuccessful) {
            repository.delete(entryEntity)
            _showSnackbarEvent.value = true
        }
    }

    // Navigation to the PlayerFragment
    private val _navigateToPlayerFragment = MutableLiveData<String>()
    val navigateToPlayerFragment
        get() = _navigateToPlayerFragment

    fun onRecordingClicked(recordingPath: String) {
        _navigateToPlayerFragment.value = recordingPath
    }

    fun onPlayerFragmentNavigated() {
        _navigateToPlayerFragment.value = null
    }

    fun cancelExporting() {
        recordingToBeExported = null
        _createAlertDialog.value = false
    }
}
