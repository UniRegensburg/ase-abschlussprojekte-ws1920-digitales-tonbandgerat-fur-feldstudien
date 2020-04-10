package de.ur.mi.audidroid.utils

import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.LabelEntity
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.viewmodels.RecordViewModel
import de.ur.mi.audidroid.views.RecordFragment
import java.util.*
import kotlin.collections.ArrayList


/**
 * The Dialog object creates a dialog depending on the parameters.
 * @author: Sabine Roth
 */

object SaveRecordingDialog {

    private lateinit var dialog: androidx.appcompat.app.AlertDialog
    private lateinit var pathTextView: TextView
    private lateinit var context: Context
    private var selectedLabels = ArrayList<String>()
    private var selectedPath: String? = null
    private lateinit var fragment: RecordFragment
    private lateinit var labelEntities: List<LabelEntity>
    private lateinit var errorTextView: TextView
    private lateinit var viewModel: RecordViewModel

    fun createDialog(
        paramContext: Context,
        layoutId: Int,
        errorMessage: String? = null,
        dataSource: Repository,
        recordViewModel: RecordViewModel,
        recordFragment: RecordFragment
    ) {
        context = paramContext
        this.viewModel = recordViewModel
        fragment = recordFragment
        val builder = MaterialAlertDialogBuilder(context)
            .setView(layoutId)
        setDialogButtons(builder)
        dataSource.getAllLabels().observe(fragment, Observer { getLabels(it) })
        dialog = builder.create()
        dialog.setOnCancelListener {
            cancelSaving()
        }
        dialog.show()
        initializeDialog(errorMessage)
    }

    private fun initializeDialog(errorMessage: String?) {
        pathTextView = dialog.findViewById<TextView>(R.id.dialog_save_recording_textview_path)!!
        selectedPath = getStoragePreference()
        dialog.findViewById<ImageButton>(R.id.dialog_save_recording_path_button)!!.setOnClickListener {
            pathButtonClicked()
        }
        getNamePreference()
        if (errorMessage != null) {
            errorTextView =
                dialog.findViewById<TextView>(R.id.dialog_save_recording_error_textview)!!
            errorTextView.text = errorMessage
            errorTextView.visibility = View.VISIBLE
        }
    }

    private fun setDialogButtons(builder: MaterialAlertDialogBuilder) {
        with(builder) {
            setPositiveButton(context.getString(R.string.dialog_save_button_text)) { _, _ ->
                saveButtonClicked()
            }
            setNegativeButton(context.getString(R.string.dialog_cancel_button_text)) { _, _ ->
               cancelSaving()
            }
        }
    }

    private fun cancelSaving(){
        selectedLabels.clear()
        viewModel.cancelDialog()
    }

    private fun saveButtonClicked() {
        var nameInput: String? =
            dialog.findViewById<EditText>(R.id.dialog_save_recording_edittext_name)!!
                .text.toString()
        nameInput = if (nameInput == "") null
        else checkVariables(nameInput!!)
        viewModel.getNewFileFromUserInput(
            nameInput,
            selectedPath,
            getLabelIdFromName()
        )
        selectedLabels.clear()
    }

    private fun getStoragePreference(): String? {
        val preferences = context.getSharedPreferences(
            context.getString(R.string.storage_preference_key),
            Context.MODE_PRIVATE
        )
        val storedPathString = preferences.getString(
            context.getString(R.string.storage_preference_key),
            context.getString(R.string.default_storage_location)
        )!!
        updateTextView(Pathfinder.getPathForView(storedPathString))
        return when (storedPathString == context.getString(R.string.default_storage_location) || storedPathString.contains(context.packageName)) {
            true -> null
            false -> storedPathString
        }
    }

    private fun pathButtonClicked() {
        Pathfinder.openPathDialog(null, context)
    }

    fun resultPathfinder(treePath: Uri) {
        if(treePath.toString().contains(context.packageName)){
            selectedPath = null
            updateTextView(context.getString(R.string.default_storage_location))
            return
        }
        val realPath = Pathfinder.getRealPath(context, treePath)
        if (realPath == null) {
            Snackbar.make(
                fragment.requireView(),
                context.resources.getString(R.string.external_sd_card_error),
                Snackbar.LENGTH_LONG
            ).show()
            return
        }
        selectedPath = realPath
        updateTextView(Pathfinder.getPathForView(realPath))
    }

    private fun updateTextView(path: String) {
        pathTextView.text = path
    }

    private fun getLabels(list: List<LabelEntity>) {
        labelEntities = list
        if (list.isNotEmpty()) {
            val chipGroup = dialog.findViewById<ChipGroup>(R.id.labelChipGroup)
            for (label in list) {
                chipGroup!!.addView(createChip(label.labelName))
            }
        } else dialog.findViewById<LinearLayout>(R.id.dialog_save_recording_label_layout)!!.visibility =
            View.GONE
    }

    private fun createChip(name: String): Chip {
        val chip = Chip(context)
        with(chip) {
            text = name
            chipBackgroundColor =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        SaveRecordingDialog.context,
                        R.color.grayed_out
                    )
                )
            setOnClickListener { labelClicked(chip) }
        }
        return chip
    }

    private fun labelClicked(clickedLabel: Chip) {
        for (string in selectedLabels) {
            if (string == (clickedLabel).text.toString()) {
                removeClickedLabel(clickedLabel)
                return
            }
        }
        addClickedLabel(clickedLabel)
    }

    private fun addClickedLabel(clickedLabel: Chip) {
        if (selectedLabels.size < context.resources.getInteger(R.integer.max_label_size)) {
            clickedLabel.chipBackgroundColor =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_primary))
            selectedLabels.add((clickedLabel).text.toString())
        } else Snackbar.make(
            fragment.requireView(),
            context.resources.getString(R.string.dialog_just_three_labels),
            Snackbar.LENGTH_LONG
        ).show()
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

    private fun getNamePreference() {
        val editText = dialog.findViewById<EditText>(R.id.dialog_save_recording_edittext_name)!!
        val preferences = context.getSharedPreferences(
            context.getString(R.string.filename_preference_key),
            Context.MODE_PRIVATE
        )
        var storedName = preferences.getString(
            context.getString(R.string.filename_preference_key),
            context.getString(R.string.filename_preference_default_value)
        )!!
        storedName = checkVariables(storedName)
        editText.setText(storedName)
        editText.setSelection(storedName.length)
    }

    private fun checkVariables(nameParam: String): String{
        var name = nameParam
        if (name.contains("{date}")) {
            name = name.replace(
                "{date}", java.lang.String.format(
                    "%s",
                    android.text.format.DateFormat.format(
                        "yyyy-MM-dd",
                        Calendar.getInstance(Locale.getDefault())
                    )
                )
            )
        }
        if (name.contains("{time}")) {
            name = name.replace(
                "{time}", java.lang.String.format(
                    "%s",
                    android.text.format.DateFormat.format(
                        "HH-mm",
                        Calendar.getInstance(Locale.getDefault())
                    )
                )
            )
        }
        return name
    }
}
