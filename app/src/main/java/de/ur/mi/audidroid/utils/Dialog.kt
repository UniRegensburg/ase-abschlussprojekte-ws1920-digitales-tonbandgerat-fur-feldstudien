package de.ur.mi.audidroid.utils

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.adapter.LabelAdapter
import de.ur.mi.audidroid.viewmodels.RecordViewModel


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
    private var selectedPath : String? = null

    fun createDialog(
        paramContext: Context,
        layoutId: Int? = null,
        textId: Int? = null,
        viewModel: RecordViewModel? = null,
        errorMessage: String? = null
    ) {
        context = paramContext
        val builder = MaterialAlertDialogBuilder(context)
        if (layoutId != null) {
            builder.setView(layoutId)
            if (errorMessage != null) {
                builder.setMessage(errorMessage)
            }
            with(builder) {
                setPositiveButton(context.getString(R.string.dialog_save_button_text)) { _, _ ->
                    var nameInput: String? =
                        dialog.findViewById<EditText>(R.id.dialog_save_recording_edittext_name)!!
                            .text.toString()
                    if (nameInput == "") nameInput = null
                    viewModel?.getNewFileFromUserInput(nameInput, selectedPath, selectedLabels)
                }
                setNeutralButton(context.getString(R.string.dialog_cancel_button_text)) { _, _ ->
                    viewModel?.cancelSaving()
                }
            }
        }
        if (textId != null) {
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
        dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        initializeDialog()
    }

    private fun initializeDialog(){
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
        val labelsList = getLabels()
        if (labelsList != null) showLabels(labelsList) else labelsRecyclerView.visibility =
            View.GONE
    }

    private fun getLabels(): ArrayList<String>? {
        //TODO get labels from user preference
        val labelsList: ArrayList<String>? = ArrayList()
        labelsList!!.add("Uni")
        labelsList.add("Arbeit")
        labelsList.add("Label3")
        labelsList.add("Label4")
        return labelsList
    }

    private fun showLabels(labelsList: ArrayList<String>) {
        //TODO: Check why it does not work with one of three smartphones
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
        println(selectedLabels)
    }

    private fun removeClickedLabel(clickedLabel: View) {
        clickedLabel.setBackgroundColor(ContextCompat.getColor(context, R.color.grayed_out))
        selectedLabels.remove((clickedLabel as MaterialButton).text.toString())
        println(selectedLabels)
    }
}
