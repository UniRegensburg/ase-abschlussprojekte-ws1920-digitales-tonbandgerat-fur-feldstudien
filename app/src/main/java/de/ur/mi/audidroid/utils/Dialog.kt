package de.ur.mi.audidroid.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.adapter.LabelAdapter
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

    fun createDialog(
        paramContext: Context,
        layoutId: Int? = null,
        textId: Int? = null,
        viewModel: RecordViewModel? = null,
        errorMessage: String? = null,
        recordFragment: RecordFragment? = null
    ) {
        context = paramContext
        if (recordFragment != null) fragment = recordFragment
        val builder = MaterialAlertDialogBuilder(context)
        if (layoutId != null) {
            builder.setView(layoutId)
            prepareDataSource()
            if (errorMessage != null) {
                builder.setMessage(errorMessage)
            }
            setDialogButtons(builder, viewModel)
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
        labelsRecyclerView =
            dialog.findViewById<RecyclerView>(R.id.dialog_save_recording_recyclerview)!!
    }

    private fun getLabels(list: List<LabelEntity>) {
        labelEntities = list
        if (list.isNotEmpty()) {
            val labelsArrayList = ArrayList<String>()
            for (label in list) {
                labelsArrayList.add(label.labelName)
            }
            showLabels(labelsArrayList)
        } else labelsRecyclerView.visibility = View.GONE
    }

    private fun showLabels(labelsList: ArrayList<String>) {
        //TODO: Use Chips
        val layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        labelsRecyclerView.layoutManager = layoutManager
        val adapter = LabelAdapter(context, labelsList, this)
        labelsRecyclerView.adapter = adapter
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

    fun labelClicked(clickedLabel: View) {
        for (string in selectedLabels) {
            if (string == (clickedLabel as MaterialButton).text.toString()) {
                removeClickedLabel(clickedLabel)
                return
            }
        }
        addClickedLabel(clickedLabel)
    }

    private fun addClickedLabel(clickedLabel: View) {
        clickedLabel.setBackgroundColor(ContextCompat.getColor(context, R.color.color_primary))
        selectedLabels.add((clickedLabel as MaterialButton).text.toString())
    }

    private fun removeClickedLabel(clickedLabel: View) {
        clickedLabel.setBackgroundColor(ContextCompat.getColor(context, R.color.grayed_out))
        selectedLabels.remove((clickedLabel as MaterialButton).text.toString())
    }

    private fun setDialogButtons(builder: MaterialAlertDialogBuilder, viewModel: RecordViewModel?) {
        with(builder) {
            setPositiveButton(context.getString(R.string.dialog_save_button_text)) { _, _ ->
                var nameInput: String? =
                    dialog.findViewById<EditText>(R.id.dialog_save_recording_edittext_name)!!
                        .text.toString()
                if (nameInput == "") nameInput = null
                viewModel?.getNewFileFromUserInput(
                    nameInput,
                    selectedPath,
                    getLabelIdFromName()
                )
            }
            setNeutralButton(context.getString(R.string.dialog_cancel_button_text)) { _, _ ->
                viewModel?.cancelSaving()
            }
        }
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

    private fun getLabelIdFromName(): ArrayList<Int>?{
        return if(selectedLabels.size!=0){
            val labelIds = ArrayList<Int>()
            for(item in selectedLabels){
                for(i in labelEntities){
                    if(item == i.labelName){
                        labelIds.add(i.uid)
                    }
                }
            }
            labelIds
        } else null
    }
}
