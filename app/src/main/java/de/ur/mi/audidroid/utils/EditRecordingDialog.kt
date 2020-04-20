package de.ur.mi.audidroid.utils


import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
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
    private lateinit var builder: MaterialAlertDialogBuilder
    private var selectedLabels = ArrayList<String>()
    private var previousLabels = ArrayList<String>()
    private var selectedPath: String? = null
    private lateinit var fragment: EditRecordingFragment
    private lateinit var dataSource: Repository
    private lateinit var labelEntities: List<LabelEntity>
    private lateinit var viewModel: EditRecordingViewModel
    private var layoutId: Int? = null
    private var recordingId: Int? = null
    private var previousRecordingName: String = ""
    private var previousPath: String = ""
    private lateinit var errorTextView: TextView
    private var previousName = MutableLiveData<Boolean>()

    fun createDialog(
        paramContext: Context,
        layoutId: Int,
        recordingId: Int,
        viewModel: EditRecordingViewModel,
        errorMessage: String? = null,
        editRecordingFragment: EditRecordingFragment,
        dataSource: Repository
    ) {
        context = paramContext
        this.dataSource = dataSource
        this.layoutId = layoutId
        this.recordingId = recordingId
        this.viewModel = viewModel
        builder = MaterialAlertDialogBuilder(context)
        fragment = editRecordingFragment
        previousName.value = true
        builder.setView(layoutId)
        prepareDataSourceAndDialog(errorMessage)
        setDialogButtons(builder)

        dialog = builder.create()
        dialog.setOnCancelListener {
            cancelSaving()
        }
        dialog.show()
    }

    private fun prepareDataSourceAndDialog(errorMessage: String?) {
        dataSource.getRecordingById(recordingId!!).observe(fragment, Observer {
            previousRecordingName = it.recordingName
            previousPath = it.recordingPath
        })
        dataSource.getRecLabelsById(recordingId!!).observe(fragment, Observer {
            for (i in it.indices) {
                previousLabels.add(it[i].labelName)
            }
            getAllLabels()
            initializeDialog(errorMessage)
        })
    }

    private fun getAllLabels() {
        dataSource.getAllLabels().observe(fragment, Observer { getLabels(it) })
    }

    private fun initializeDialog(errorMessage: String?) {
        pathTextView = dialog.findViewById<TextView>(R.id.dialog_save_recording_textview_path)!!
        if (previousPath.contains(context.packageName)) {
            pathTextView.text = context.getString(R.string.default_storage_location)
        } else {
            pathTextView.text = previousPath.replace("/$previousRecordingName.aac", "")
        }
        selectedPath = previousPath.replace("/$previousRecordingName.aac", "")
        dialog.findViewById<ImageButton>(R.id.dialog_save_recording_path_button)!!
            .setOnClickListener {
                pathButtonClicked()
            }
        getRecordingName()
        if (errorMessage != null) {
            errorTextView =
                dialog.findViewById<TextView>(R.id.dialog_save_recording_error_textview)!!
            errorTextView.text = errorMessage
            errorTextView.visibility = View.VISIBLE
        }
    }

    private fun setDialogButtons(builder: MaterialAlertDialogBuilder) {
        with(builder) {
            setPositiveButton(context.getString(R.string.dialog_update_button_text)) { _, _ ->
                val editText =
                    dialog.findViewById<EditText>(R.id.dialog_save_recording_edittext_name)!!
                if (editText.text.toString() == previousRecordingName) {
                    updateButtonClicked()
                } else {
                    saveButtonClicked()
                }
            }
            setNegativeButton(context.getString(R.string.dialog_cancel_button_text)) { _, _ ->
                cancelSaving()
            }
        }
    }

    private fun cancelSaving() {
        selectedLabels.clear()
        previousLabels.clear()
        viewModel.cancelSaving()
    }

    private fun saveButtonClicked() {
        var nameInput: String? =
            dialog.findViewById<EditText>(R.id.dialog_save_recording_edittext_name)!!
                .text.toString()
        nameInput = if (nameInput == "") null
        else checkVariables(nameInput!!)
        viewModel.saveNewRecording(
            nameInput,
            selectedPath,
            getLabelIdFromName()
        )
        selectedLabels.clear()
        previousLabels.clear()
    }

    private fun updateButtonClicked() {
        val nameInput: String =
            dialog.findViewById<EditText>(R.id.dialog_save_recording_edittext_name)!!.text.toString()
        viewModel.updatePreviousRecording(
            nameInput,
            previousPath,
            selectedPath,
            getLabelIdFromName()
        )
        selectedLabels.clear()
        previousLabels.clear()
    }

    private fun pathButtonClicked() {
        Pathfinder.openPathDialog(null, context, "EditRecordingFragment")
    }

    fun resultPathfinder(treePath: Uri) {
        if (treePath.toString().contains(context.packageName)) {
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
        updateTextView(Pathfinder.getShortenedPath(realPath))
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
            if (previousLabels.contains(name)) {
                chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        EditRecordingDialog.context,
                        R.color.color_primary
                    )
                )
                setTextColor(
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            context,
                            R.color.color_on_primary
                        )
                    )
                )
                selectedLabels.add(name)
            } else {
                chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        EditRecordingDialog.context,
                        R.color.grayed_out
                    )
                )
            }
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
            clickedLabel.setTextColor(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        context,
                        R.color.color_on_primary
                    )
                )
            )
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
        clickedLabel.setTextColor(
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    R.color.color_on_background
                )
            )
        )
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

    private fun getRecordingName() {
        val editText = dialog.findViewById<EditText>(R.id.dialog_save_recording_edittext_name)!!
        editText.setText(previousRecordingName)
        editText.setSelection(previousRecordingName.length)
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString() == previousRecordingName) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).text =
                        context.getString(R.string.dialog_update_button_text)
                } else {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).text =
                        context.getString(R.string.dialog_save_button_text)
                }
            }
        })
    }

    private fun checkVariables(nameParam: String): String {
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
