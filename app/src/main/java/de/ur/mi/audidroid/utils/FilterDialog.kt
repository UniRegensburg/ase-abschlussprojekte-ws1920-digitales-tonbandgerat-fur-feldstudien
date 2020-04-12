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
    private var selectedPath: String? = null
    private lateinit var fragment: FilesFragment
    private lateinit var labelEntities: List<LabelEntity>
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
        with(builder){
            setPositiveButton(context.getString(R.string.menu_filter)){_, _ ->
                val labels = getLabelIdFromName()
                println("Positiver Button geklickt")
                viewModel.setFilterResult(labels)
            }
        }

        dataSource.getAllLabels().observe(fragment, Observer { getLabels(it) })
        dialog = builder.create()
        dialog.setOnCancelListener {
            cancelSaving()
        }
        dialog.show()
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
                        context,
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

    private fun cancelSaving(){
        selectedLabels.clear()
        viewModel.cancelFilterDialog()
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