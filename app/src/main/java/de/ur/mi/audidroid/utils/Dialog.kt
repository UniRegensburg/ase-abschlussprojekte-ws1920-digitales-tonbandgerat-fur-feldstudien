package de.ur.mi.audidroid.utils

import android.app.Activity
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

object Dialog {

    private lateinit var dialog: androidx.appcompat.app.AlertDialog
    private lateinit var pathTextView: TextView
    private lateinit var context: Context
    private var selectedLabels = ArrayList<String>()
    private var selectedPath: String? = null
    private lateinit var fragment: RecordFragment
    private lateinit var dataSource: Repository
    private lateinit var labelEntities: List<LabelEntity>
    private lateinit var viewModel: RecordViewModel
    private var layoutId: Int? = null

    fun createDialog(
        paramContext: Context,
        layoutId: Int? = null,
        textId: Int? = null,
        viewModel: RecordViewModel? = null,
        errorMessage: String? = null,
        recordFragment: RecordFragment? = null
    ) {
        context = paramContext
        if (layoutId != null) this.layoutId = layoutId
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
        selectedPath = getStoragePreference()
        dialog.findViewById<ImageButton>(R.id.dialog_save_recording_path_button)!!.setOnClickListener {
            pathButtonClicked()
        }
        getNamePreference()
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
        val preferences = context.getSharedPreferences(
            context.getString(R.string.storage_preference_key),
            Context.MODE_PRIVATE
        )
        val storedPathString = preferences.getString(
            context.getString(R.string.storage_preference_key),
            context.getString(R.string.default_storage_location)
        )!!
        updateTextView(storedPathString)
        return when (storedPathString == context.getString(R.string.default_storage_location)) {
            true -> null
            false -> storedPathString
        }
    }

    private fun pathButtonClicked() {
        Pathfinder.openPathDialog(null, context)
    }

    fun resultPathfinder(treePath: Uri?) {
        if (treePath == null) {
            //createDialog(paramContext = context, layoutId = layoutId, textId = null, errorMessage =  context.resources.getString(R.string.external_sd_card_error), recordFragment = fragment)
            Snackbar.make(
                fragment.requireView(),
                context.resources.getString(R.string.external_sd_card_error),
                Snackbar.LENGTH_LONG
            ).show()
            return
        }
        val realPath = Pathfinder.getRealPath(context, treePath)!!
        selectedPath = realPath
        updateTextView(realPath)
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
                ColorStateList.valueOf(ContextCompat.getColor(Dialog.context, R.color.grayed_out))
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

    private fun getNamePreference(){
        val editText = dialog.findViewById<EditText>(R.id.dialog_save_recording_edittext_name)!!
        val preferences = context.getSharedPreferences(
            context.getString(R.string.filename_preference_key),
            Context.MODE_PRIVATE
        )
        var storedName = preferences.getString(
            context.getString(R.string.filename_preference_key),
            context.getString(R.string.filename_preference_default_value)
        )!!
        if(storedName.contains("{date}")){
            storedName = storedName.replace("{date}",  java.lang.String.format(
                "%s",
                android.text.format.DateFormat.format(
                    "yyyy-MM-dd",
                    Calendar.getInstance(Locale.getDefault())
                )))
        }
        if(storedName.contains("{time}")){
            storedName = storedName.replace("{time}", java.lang.String.format(
                "%s",
                android.text.format.DateFormat.format(
                    "HH-mm",
                    Calendar.getInstance(Locale.getDefault())
                )))
        }
        editText.setText(storedName)
        editText.setSelection(storedName.length)
    }
}
