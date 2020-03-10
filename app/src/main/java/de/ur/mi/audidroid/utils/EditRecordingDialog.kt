package de.ur.mi.audidroid.utils


import android.content.Context
import android.content.res.ColorStateList
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
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.LabelEntity
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.viewmodels.EditRecordingViewModel
import de.ur.mi.audidroid.views.EditRecordingFragment
import java.util.*
import kotlin.collections.ArrayList

object EditRecordingDialog {

    private lateinit var dialog: androidx.appcompat.app.AlertDialog
    private lateinit var pathTextView: TextView
    private lateinit var context: Context
    private var selectedLabels = ArrayList<String>()
    private var selectedPath: String? = null
    private lateinit var fragment: EditRecordingFragment
    private lateinit var dataSource: Repository
    private lateinit var labelEntities: List<LabelEntity>
    private lateinit var viewModel: EditRecordingViewModel
    private var layoutId: Int? = null
    private lateinit var errorTextView: TextView

    fun createDialog(
        paramContext: Context,
        layoutId: Int,
        viewModel: EditRecordingViewModel,
        errorMessage: String? = null,
        editRecordingFragment: EditRecordingFragment,
        dataSource: Repository
    ) {
        context = paramContext
        this.dataSource = dataSource
        this.layoutId = layoutId
        this.viewModel = viewModel
        fragment = editRecordingFragment
        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(layoutId)
        prepareDataSource()
        setDialogButtons(builder)

        dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        initializeDialog(errorMessage)
    }

    private fun prepareDataSource() {
        dataSource.getAllLabels().observe(fragment, Observer { getLabels(it) })
    }

    private fun initializeDialog(errorMessage: String?) {
        pathTextView = dialog.findViewById<TextView>(R.id.dialog_save_recording_textview_path)!!
        selectedPath = getStoragePreference()
        dialog.findViewById<ImageButton>(R.id.dialog_save_recording_path_button)!!
            .setOnClickListener {
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
                        EditRecordingDialog.context,
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
        if (storedName.contains("{date}")) {
            storedName = storedName.replace(
                "{date}", java.lang.String.format(
                    "%s",
                    android.text.format.DateFormat.format(
                        "yyyy-MM-dd",
                        Calendar.getInstance(Locale.getDefault())
                    )
                )
            )
        }
        if (storedName.contains("{time}")) {
            storedName = storedName.replace(
                "{time}", java.lang.String.format(
                    "%s",
                    android.text.format.DateFormat.format(
                        "HH-mm",
                        Calendar.getInstance(Locale.getDefault())
                    )
                )
            )
        }
        editText.setText(storedName)
        editText.setSelection(storedName.length)
    }
}
