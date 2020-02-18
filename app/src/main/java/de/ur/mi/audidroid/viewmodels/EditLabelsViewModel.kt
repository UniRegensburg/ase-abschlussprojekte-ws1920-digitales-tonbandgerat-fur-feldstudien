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

class EditLabelsViewModel(dataSource: Repository, application: Application) : AndroidViewModel(application) {

    private val repository = dataSource
    private val context = getApplication<Application>().applicationContext
    private lateinit var frameLayout: FrameLayout
    val res: Resources = context.resources
    val allLabels: LiveData<List<LabelEntity>> = repository.getAllLabels()
    private val _createDialog = MutableLiveData<Boolean>()
    private var _showSnackbarEvent = MutableLiveData<Boolean>()
    var errorMessage: String? = null

    fun initializeLayout(layout: FrameLayout) {
        frameLayout = layout
    }

    val createDialog: MutableLiveData<Boolean>
        get() = _createDialog

    val showSnackbarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = null
    }

    val empty: LiveData<Boolean> = Transformations.map(allLabels) {
        it.isEmpty()
    }

    fun onLabelClicked(labelEntity: LabelEntity) {
        Log.d("label", "label clicked")
    }

    fun onLabelDeleteClicked(labelEntity: LabelEntity) {
        showSnackBar(R.string.label_deleted)
    }

    fun onLabelSaveClicked(nameInput: String?) {
        if(!validName(nameInput)) {
            return
        }
        _createDialog.value = false
        insertLabelIntoDB(nameInput!!)
    }

    fun validName(name: String?): Boolean {
        if(name == null) {
            errorMessage = R.string.label_name_nonexistent.toString()
            return false
        }
        if(name.length > 10) {
            errorMessage = R.string.label_name_too_long.toString()
            return false
        }
        return true
    }

    fun insertLabelIntoDB(labelName: String) {
        val newLabel = LabelEntity(0, labelName)
        repository.insertLabel(newLabel)
    }

    fun requestLabelDialog() {
        _createDialog.value = true
    }

    fun cancelSaving() {
        errorMessage = null
        _createDialog.value = false
    }

    private fun showSnackBar(text: Int) {
        Snackbar.make(frameLayout, text, Snackbar.LENGTH_LONG).show()
    }
}
