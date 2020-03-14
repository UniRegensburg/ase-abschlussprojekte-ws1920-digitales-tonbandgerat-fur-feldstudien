package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.content.res.Resources
import android.util.Log
import android.widget.FrameLayout
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.MarkerEntity
import de.ur.mi.audidroid.models.Repository
import java.util.regex.Pattern

class EditMarkersViewModel(dataSource: Repository, application: Application) :
    AndroidViewModel(application) {

    private val repository = dataSource
    private val context = getApplication<Application>().applicationContext
    private lateinit var frameLayout: FrameLayout
    val res: Resources = context.resources
    val allMarkers: LiveData<List<MarkerEntity>> = repository.getAllMarkers()
    private val _createAlertDialog = MutableLiveData<Boolean>()
    private val _createConfirmDialog = MutableLiveData<Boolean>()
    private var _showSnackbarEvent = MutableLiveData<Boolean>()
    var errorMessage: String? = null
    var markerToBeEdited: MarkerEntity? = null

    fun initializeLayout(layout: FrameLayout) {
        frameLayout = layout
    }

    val createAlertDialog: MutableLiveData<Boolean>
        get() = _createAlertDialog

    val createConfirmDialog: MutableLiveData<Boolean>
        get() = _createConfirmDialog

    val showSnackbarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = null
    }

    val empty: LiveData<Boolean> = Transformations.map(allMarkers) {
        it.isEmpty()
    }

    fun onMarkerClicked(markerEntity: MarkerEntity) {
        markerToBeEdited = markerEntity
        _createAlertDialog.value = true
    }

    fun onMarkerDeleteClicked(markerEntity: MarkerEntity) {
        markerToBeEdited = markerEntity
        _createConfirmDialog.value = true
    }

    fun onMarkerSaveClicked(nameInput: String?) {
        if(nameInput==null){
            return
        }
        if (markerNameAlreadyTaken(nameInput)) {
            errorMessage = res.getString(R.string.dialog_marker_already_exists)
            _createAlertDialog.value = true
            return
        }
        if (!validName(nameInput)) {
            errorMessage = res.getString(R.string.dialog_label_invalid_name)
            _createAlertDialog.value = true
            return
        }
        errorMessage = null
        _createAlertDialog.value = false
        insertMarkerIntoDB(nameInput)
    }

    fun onMarkerUpdateClicked(nameInput: String?, markerEntity: MarkerEntity) {
        _createAlertDialog.value = false
        if (!validName(nameInput)) {
            errorMessage = res.getString(R.string.dialog_marker_invalid_name)
            _createAlertDialog.value = true
            return
        }
        updateMarkerInDB(nameInput!!, markerEntity)
    }

    fun validName(name: String?): Boolean {
        val labelName = name ?: ""
        return Pattern.compile("^[a-zA-Z0-9_-]{1,10}$").matcher(labelName).matches()
    }

    private fun markerNameAlreadyTaken(markerName: String): Boolean {
        return repository.getMarkerByName(markerName).isNotEmpty()
    }

    fun insertMarkerIntoDB(markerName: String) {
        val newMarker = MarkerEntity(0, markerName)
        repository.insertMarker(newMarker)
        showSnackBar(String.format(context.getString(R.string.marker_inserted), newMarker.markerName))
    }

    fun updateMarkerInDB(markerName: String, markerEntity: MarkerEntity) {
        val updatedMarker = MarkerEntity(markerEntity.uid, markerName)
        repository.updateMarker(updatedMarker)
        markerToBeEdited = null
        showSnackBar(
            String.format(
                context.getString(R.string.marker_updated),
                updatedMarker.markerName
            )
        )
    }

    fun deleteMarkerFromDB(markerEntity: MarkerEntity) {
        repository.deleteMarker(markerEntity)
        showSnackBar(
            String.format(
                context.getString(R.string.marker_deleted),
                markerToBeEdited!!.markerName
            )
        )
        markerToBeEdited = null
    }

    fun requestMarkerDialog() {
        if (repository.getMarkerCount() < 6) {
            _createAlertDialog.value = true
        } else {
            showSnackBar(res.getString(R.string.max_markers_created))
        }
    }

    fun cancelSaving() {
        errorMessage = null
        markerToBeEdited = null
        _createAlertDialog.value = false
    }

    private fun showSnackBar(text: String) {
        Snackbar.make(frameLayout, text, Snackbar.LENGTH_LONG).show()
    }

}
