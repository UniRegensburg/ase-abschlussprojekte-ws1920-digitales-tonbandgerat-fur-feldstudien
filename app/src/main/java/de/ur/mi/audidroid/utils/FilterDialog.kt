package de.ur.mi.audidroid.utils

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
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
import de.ur.mi.audidroid.views.FilesFragment

object FilterDialog {

    private lateinit var dialog: androidx.appcompat.app.AlertDialog
    private lateinit var context: Context
    private var selectedLabels = ArrayList<String>()
    private var selectedMarks = ArrayList<String>()
    private var nameInput : String? = null
    private lateinit var fragment: FilesFragment
    private lateinit var labelEntities: List<LabelEntity>
    private lateinit var markEntities: List<MarkerEntity>
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
        with(builder){
            setPositiveButton(context.getString(R.string.menu_filter)){_, _ ->
                nameInput = dialog.findViewById<EditText>(R.id.dialog_filter_recording_edittext_name)!!.text.toString()
                viewModel.setFilterResult(selectedLabels, selectedMarks, nameInput)
                cancelDialog()
            }
            setNegativeButton(R.string.filter_clear){_,_->
                cancelDialog()
                clearDialog()
                viewModel.clearFilter()
            }
        }

        dataSource.getAllLabels().observe(fragment, Observer { getLabels(it) })
        dataSource.getAllMarkers().observe(fragment, Observer { getMarks(it) })
        dialog = builder.create()
        dialog.setOnCancelListener { cancelDialog() }
        dialog.show()
        nameInput?.let { dialog.findViewById<EditText>(R.id.dialog_filter_recording_edittext_name)!!.setText(nameInput) }
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
            if (type == R.integer.chip_type_label){
                chipBackgroundColor = setChipBackground(selectedLabels.contains(name))
                setTextColor(setChipTextColor(selectedLabels.contains(name)))
                setOnClickListener {
                    labelClicked(chip) }
            }else{
                chipBackgroundColor = setChipBackground(selectedMarks.contains(name))
                setTextColor(setChipTextColor(selectedMarks.contains(name)))
                setOnClickListener {
                    markClicked(chip) }
            }
        }
        return chip
    }

    private fun setChipBackground(preSelected: Boolean):ColorStateList{
        return if (preSelected){
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_primary))
        }else{
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grayed_out))
        }
    }

    private fun setChipTextColor(preSelected: Boolean): ColorStateList{
        return if (preSelected){
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_on_primary))
        }else{
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_on_background))
        }
    }

    private fun markClicked(clickedMark: Chip) {
        for (string in selectedMarks) {
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

    private fun addClickedMark(clickedMark: Chip) {
        clickedMark.chipBackgroundColor =
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_primary))
        clickedMark.setTextColor(
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    R.color.color_on_primary
                )
            )
        )
        selectedMarks.add((clickedMark).text.toString())
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

    private fun removeClickedMark(clickedMark: Chip) {
        clickedMark.chipBackgroundColor =
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grayed_out))
        clickedMark.setTextColor(
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    R.color.color_on_background
                )
            )
        )
        selectedMarks.remove((clickedMark).text.toString())
    }

    fun clearDialog(){
        selectedLabels.clear()
        selectedMarks.clear()
        nameInput = null
    }

    private fun cancelDialog(){
        viewModel.cancelFilterDialog()
    }
}
