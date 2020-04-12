package de.ur.mi.audidroid.utils

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
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
import de.ur.mi.audidroid.models.MarkerEntity
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.viewmodels.FilesViewModel
import de.ur.mi.audidroid.viewmodels.RecordViewModel
import de.ur.mi.audidroid.views.FilesFragment
import de.ur.mi.audidroid.views.RecordFragment

object FilterDialog {

    private lateinit var dialog: androidx.appcompat.app.AlertDialog
    private lateinit var pathTextView: TextView
    private lateinit var context: Context
    private var selectedLabels = ArrayList<String>()
    private var selectedMarks = ArrayList<String>()
    private var selectedPath: String? = null
    private lateinit var fragment: FilesFragment
    private lateinit var labelEntities: List<LabelEntity>
    private lateinit var markEntities: List<MarkerEntity>
    private lateinit var errorTextView: TextView
    private lateinit var viewModel: FilesViewModel

    fun createDialog(
        context: Context,
        layoutId: Int,
        viewModel: FilesViewModel,
        dataSource: Repository,
        fragment: FilesFragment
    ) {
        this.viewModel = viewModel
        this.context = context
        val builder = MaterialAlertDialogBuilder(context).setView(layoutId)
        selectedLabels.clear()
        selectedMarks.clear()
        with(builder){
            setPositiveButton(context.getString(R.string.menu_filter)){_, _ ->

                viewModel.setFilterResult(selectedLabels)

            }
        }

        dataSource.getAllLabels().observe(fragment, Observer { getLabels(it) })
        dataSource.getAllMarkers().observe(fragment, Observer { getMarks(it) })
        dialog = builder.create()
        dialog.setOnCancelListener {
            cancelSaving()
        }
        dialog.show()
    }

    private fun getMarks(list: List<MarkerEntity>) {
        markEntities = list
        if (list.isNotEmpty()) {
            val chipGroup = dialog.findViewById<ChipGroup>(R.id.markChipGroup)
            for (mark in list) {
                chipGroup!!.addView(createChip(mark.markerName, R.integer.chip_type_mark))
            }
        } else dialog.findViewById<LinearLayout>(R.id.dialog_filter_recording_mark_layout)!!.visibility =
            View.GONE
    }

    private fun getLabels(list: List<LabelEntity>) {
        labelEntities = list
        if (list.isNotEmpty()) {
            val chipGroup = dialog.findViewById<ChipGroup>(R.id.labelChipGroup)
            for (label in list) {
                chipGroup!!.addView(createChip(label.labelName, R.integer.chip_type_label))
            }
        } else dialog.findViewById<LinearLayout>(R.id.dialog_filter_recording_label_layout)!!.visibility =
            View.GONE
    }

    private fun createChip(name: String, type: Int): Chip {
        val chip = Chip(context)
        with(chip) {
            text = name
            chipBackgroundColor =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        context,
                        R.color.grayed_out
                    )
                )
            if (type == R.integer.chip_type_label){
                setOnClickListener {
                    labelClicked(chip) }
            }else{
                setOnClickListener {
                    markClicked(chip) }
            }

        }
        return chip
    }

    private fun markClicked(clickedMark: Chip) {
        for (string in selectedLabels) {
            if (string == (clickedMark).text.toString()) {
                removeClickedMark(clickedMark)
                return
            }
        }
        addClickedMark(clickedMark)
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

    private fun addClickedMark(clickedMark: Chip) {
        clickedMark.chipBackgroundColor =
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_primary))
        selectedMarks.add((clickedMark).text.toString())
    }

    private fun removeClickedLabel(clickedLabel: Chip) {
        clickedLabel.chipBackgroundColor =
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grayed_out))
        selectedLabels.remove((clickedLabel).text.toString())
    }

    private fun removeClickedMark(clickedMark: Chip) {
        clickedMark.chipBackgroundColor =
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grayed_out))
        selectedMarks.remove((clickedMark).text.toString())
    }

    private fun cancelSaving(){
        selectedLabels.clear()
        viewModel.cancelFilterDialog()
    }
}