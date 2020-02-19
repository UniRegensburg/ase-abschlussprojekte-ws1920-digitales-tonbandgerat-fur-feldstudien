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
import de.ur.mi.audidroid.models.LabelEntity
import de.ur.mi.audidroid.models.Repository
import java.util.regex.Pattern

class EditLabelsViewModel(dataSource: Repository, application: Application) : AndroidViewModel(application) {

    private val repository = dataSource
    private val context = getApplication<Application>().applicationContext
    private lateinit var frameLayout: FrameLayout
    val res: Resources = context.resources
    val allLabels: LiveData<List<LabelEntity>> = repository.getAllLabels()
    private val _createAlertDialog = MutableLiveData<Boolean>()
    private val _createConfirmDialog = MutableLiveData<Boolean>()
    private var _showSnackbarEvent = MutableLiveData<Boolean>()
    var errorMessage: String? = null
    var labelToBeEdited: LabelEntity? = null

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

    val empty: LiveData<Boolean> = Transformations.map(allLabels) {
        it.isEmpty()
    }

    fun onLabelClicked(labelEntity: LabelEntity) {
        labelToBeEdited = labelEntity
        _createAlertDialog.value = true
    }

    fun onLabelDeleteClicked(labelEntity: LabelEntity) {
        labelToBeEdited = labelEntity
        _createConfirmDialog.value = true
    }

    fun onLabelSaveClicked(nameInput: String?) {
        if(!validName(nameInput)) {
            errorMessage = res.getString(R.string.dialog_invalid_name)
            _createAlertDialog.value = true
            return
        }
        _createAlertDialog.value = false
        insertLabelIntoDB(nameInput!!)
    }

    fun onLabelUpdateClicked(nameInput: String?, labelEntity: LabelEntity) {
        _createAlertDialog.value = false
        if(!validName(nameInput)) {
            errorMessage = res.getString(R.string.dialog_invalid_name)
            _createAlertDialog.value = true
            return
        }
        updateLabelInDB(nameInput!!, labelEntity)
    }

    fun validName(name: String?): Boolean {
        val labelName = name ?: ""
        return Pattern.compile("^[a-zA-Z0-9_-]{1,10}$").matcher(labelName).matches()
    }

    fun insertLabelIntoDB(labelName: String) {
        val newLabel = LabelEntity(0, labelName)
        repository.insertLabel(newLabel)
        showSnackBar(R.string.label_inserted)
    }

    fun updateLabelInDB(labelName: String, labelEntity: LabelEntity) {
        val updatedLabel = LabelEntity(labelEntity.uid, labelName)
        repository.updateLabel(updatedLabel)
        labelToBeEdited = null
        showSnackBar(R.string.label_updated)
    }

    fun deleteLabelFromDB(labelEntity: LabelEntity) {
        repository.deleteLabel(labelEntity)
        labelToBeEdited = null
        showSnackBar(R.string.label_deleted)
    }

    fun requestLabelDialog() {
        _createAlertDialog.value = true
    }

    fun cancelSaving() {
        errorMessage = null
        _createAlertDialog.value = false
    }

    private fun showSnackBar(text: Int) {
        Snackbar.make(frameLayout, text, Snackbar.LENGTH_LONG).show()
    }
}
