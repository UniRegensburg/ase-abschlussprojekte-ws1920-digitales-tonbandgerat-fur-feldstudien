package de.ur.mi.audidroid.utils

import android.content.Context
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.ur.mi.audidroid.viewmodels.RecordViewModel
import de.ur.mi.audidroid.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.*


/**
 * The Dialog object creates a dialog depending on the parameters.
 * @author: Sabine Roth
 */

object Dialog {

    private lateinit var dialog: androidx.appcompat.app.AlertDialog
    private lateinit var pathTextView: TextView
    private lateinit var labelsListView: ListView
    private lateinit var context: Context

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
                    viewModel?.getNewFileFromUserInput(nameInput, pathTextView.text.toString())
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

        pathTextView = dialog.findViewById<TextView>(R.id.dialog_save_recording_textview_path)!!
        if (getStoragePreference() != null) {
            pathTextView.text = getStoragePreference()
        }
        dialog.findViewById<ImageButton>(R.id.dialog_save_recording_path_button)!!.setOnClickListener {
            pathButtonClicked()
        }
        labelsListView = dialog.findViewById<ListView>(R.id.dialog_save_recording_labelslist)!!
        val labelsList = getLabels()
        if(labelsList != null) showLabels(labelsList) else labelsListView.visibility = View.GONE
    }

    private fun getLabels(): ArrayList<String>? {
        //TODO get labels from user preference
        //TODO: Maybe convert directlly to an array and don't use the for loop
        val labelsList: ArrayList<String>? = ArrayList()
        labelsList!!.add("uni")
        labelsList.add("Arbeit")
        return labelsList
    }

    private fun showLabels(labelsList: ArrayList<String>) {
        val labelsArray = arrayOfNulls<String>(labelsList.size)
        for (i in 0 until labelsList.size){
            labelsArray[i] = labelsList[i]
        }
        //TODO: Change look of list

       /* val layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRecyclerView = findViewById(R.id.recycler_view) as RecyclerView
        mRecyclerView.setLayoutManager(layoutManager)*/
        
        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, labelsArray)
        labelsListView.adapter = adapter
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
        //Change textview if new location is chosen
    }
}
