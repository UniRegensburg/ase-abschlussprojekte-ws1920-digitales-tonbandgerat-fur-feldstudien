package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupMenu
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.Repository
import java.io.File

/**
 * ViewModel for FilesFragment.
 * @author: Theresa Strohmeier
 */
class FilesViewModel(dataSource: Repository, application: Application) :
    AndroidViewModel(application) {

    private val repository = dataSource
    private val context = getApplication<Application>().applicationContext
    val allRecordings: LiveData<List<EntryEntity>> = repository.getAllRecordings()
    private lateinit var frameLayout: FrameLayout

    private var _showSnackbarEvent = MutableLiveData<Boolean>()

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
            }
            true
        }
        popupMenu.show()
    }

    private fun delete(entryEntity: EntryEntity) {
        var file = File(entryEntity.recordingPath)
        if (file.exists()) {
            if (file.delete()) {
                if (repository.delete(entryEntity) == 1) {
                    _showSnackbarEvent.value = true
                } else {
                    showSnackBar(R.string.error_message_file_cannot_be_deleted)
                }
            } else {
                showSnackBar(R.string.error_message_file_cannot_be_deleted)
            }
        } else {
            showSnackBar(R.string.error_message_path_does_not_exist)
        }
    }

    fun initializeFrameLayout(frameLayout: FrameLayout) {
        this.frameLayout = frameLayout
    }

    private fun showSnackBar(text: Int) {
        Snackbar.make(frameLayout, text, Snackbar.LENGTH_LONG).show()
    }

    // Navigation to the PlayerFragment
    private val _navigateToPlayerFragment = MutableLiveData<Int>()
    val navigateToPlayerFragment
        get() = _navigateToPlayerFragment

    fun onRecordingClicked(recordingId: Int) {
        _navigateToPlayerFragment.value = recordingId
    }

    fun onPlayerFragmentNavigated() {
        _navigateToPlayerFragment.value = null
    }
}
