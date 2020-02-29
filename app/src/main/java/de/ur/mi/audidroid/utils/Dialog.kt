package de.ur.mi.audidroid.utils

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.LabelEntity
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.viewmodels.RecordViewModel
import de.ur.mi.audidroid.views.RecordFragment


/**
 * The Dialog object creates a dialog depending on the parameters.
 * @author: Sabine Roth
 */

object Dialog {

    private lateinit var dialog: androidx.appcompat.app.AlertDialog
    private lateinit var pathTextView: TextView
    private lateinit var labelsRecyclerView: RecyclerView
    private lateinit var context: Context
    private var selectedLabels = ArrayList<String>()
    private var selectedPath: String? = null
    private lateinit var fragment: RecordFragment
    private lateinit var dataSource: Repository
    private lateinit var labelEntities: List<LabelEntity>
    private lateinit var viewModel: RecordViewModel

    fun createDialog(
        paramContext: Context,
        layoutId: Int? = null,
        textId: Int? = null,
        viewModel: RecordViewModel? = null,
        errorMessage: String? = null,
        recordFragment: RecordFragment? = null
    ) {
        context = paramContext
        if (viewModel != null) this.viewModel = viewModel
        if (recordFragment != null) fragment = recordFragment
        val builder = MaterialAlertDialogBuilder(context)
        if (layoutId != null) {
            builder.setView(layoutId)
            prepareDataSource()
            if (errorMessage != null) {
                builder.setMessage(errorMessage)
            }
            setDialogButtons(builder)
        }
        if (textId != null) {
            createPermissionDialog(builder, textId)
        }
        dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        initializeDialog()
    }

    private fun prepareDataSource() {
        dataSource = Repository((context as Activity).application)
        dataSource.getAllLabels().observe(fragment, Observer { getLabels(it) })
    }

    private fun initializeDialog() {
        pathTextView = dialog.findViewById<TextView>(R.id.dialog_save_recording_textview_path)!!
        val storedPath = getStoragePreference()
        if (storedPath != null) {
            pathTextView.text = storedPath
            selectedPath = storedPath
        }
        dialog.findViewById<ImageButton>(R.id.dialog_save_recording_path_button)!!.setOnClickListener {
            pathButtonClicked()
        }
    }

    private fun setDialogButtons(builder: MaterialAlertDialogBuilder) {
        with(builder) {
            setPositiveButton(context.getString(R.string.dialog_save_button_text)) { _, _ ->
                saveButtonClicked()
            }
            setNeutralButton(context.getString(R.string.dialog_cancel_button_text)) { _, _ ->
                viewModel.cancelSaving()
            }
        }
    }

    private fun saveButtonClicked() {
        var nameInput: String? =
            dialog.findViewById<EditText>(R.id.dialog_save_recording_edittext_name)!!
                .text.toString()
        if (nameInput == "") nameInput = null
        viewModel.getNewFileFromUserInput(
            nameInput,
            selectedPath,
            getLabelIdFromName()
        )
        selectedLabels.clear()
        selectedPath = null
    }

    private fun createPermissionDialog(builder: MaterialAlertDialogBuilder, textId: Int) {
        with(builder) {
            setTitle(R.string.permission_title)
            setMessage(textId)
            setPositiveButton(
                R.string.permission_button
            ) { _, _ ->
                PermissionHelper.makeRequest(context)
            }
        }
    }

    private fun getStoragePreference(): String? {
        /*private fun getStoragePreference(): Uri{
            val preferences = context!!.getSharedPreferences(res.getString(R.string.storage_preference_key), Context.MODE_PRIVATE)
            return Uri.parse(preferences.getString(res.getString(R.string.storage_preference_key),"default"))
        }

        private fun initializeTmpFile(): FileDescriptor{
            val preferredDir = DocumentFile.fromTreeUri(context!!, getStoragePreference())!!
            tmpFile = preferredDir.createFile("acc",res.getString(R.string.suffix_temp_file))!!
            return context.contentResolver.openFileDescriptor(tmpFile.uri, "rwt")!!.fileDescriptor
        }*/
        return null
    }

    private fun pathButtonClicked() {
        //TODO: Add folder-clicking
    }

    private fun getLabels(list: List<LabelEntity>) {
        labelEntities = list
        if (list.isNotEmpty()) {
            val labelsArrayList = ArrayList<String>()
            for (label in list) {
                labelsArrayList.add(label.labelName)
            }
            showLabels(labelsArrayList)
        } else {
            labelsRecyclerView.visibility = View.GONE
            dialog.findViewById<TextView>(R.id.dialog_save_recording_textview_labels)!!.visibility =
                View.GONE
        }
    }

    private fun showLabels(labelsList: ArrayList<String>) {
        val chipGroup = dialog.findViewById<ChipGroup>(R.id.labelChipGroup)
        for (item in labelsList) {
            val chip = Chip(context)
            chip.text = item
            chip.chipBackgroundColor =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grayed_out))
            chip.setOnClickListener { labelClicked(chip) }
            chipGroup!!.addView(chip)
        }
    }

    fun labelClicked(clickedLabel: Chip) {
        for (string in selectedLabels) {
            if (string == (clickedLabel).text.toString()) {
                removeClickedLabel(clickedLabel)
                return
            }
        }
        addClickedLabel(clickedLabel)
    }

    private fun addClickedLabel(clickedLabel: Chip) {
        clickedLabel.chipBackgroundColor =
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_primary))
        selectedLabels.add((clickedLabel).text.toString())
    }

    private fun removeClickedLabel(clickedLabel: Chip) {
        clickedLabel.chipBackgroundColor =
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grayed_out))
        selectedLabels.remove((clickedLabel).text.toString())
    }

    private fun getLabelIdFromName(): ArrayList<Int>? {
        return if (selectedLabels.size != 0) {
            val labelIds = ArrayList<Int>()
            for (item in selectedLabels) {
                for (i in labelEntities) {
                    if (item == i.labelName) {
                        labelIds.add(i.uid)
                    }
                }
            }
            labelIds
        } else null
    }
}
