package de.ur.mi.audidroid.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.viewmodels.FilesViewModel

object SearchDialog {

    private lateinit var dialog: AlertDialog

    fun createDialog(
        context: Context,
        layoutId: Int,
        viewModel: FilesViewModel
    ){
        val builder = AlertDialog.Builder(context)
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val dialogView: View = inflater.inflate(layoutId, null)
        val editText: EditText = dialogView.findViewById(R.id.dialog_search_edit_text)
        val pos: Int = editText.text.length
        editText.requestFocus()
        editText.setSelection(pos)
        KeyboardHelper.showSoftKeyboard(editText)
        builder.setView(dialogView)
        with(builder){
            setPositiveButton(context.getString(R.string.dialog_search)){_,_ ->
                val searchInput: String? = editText.text.toString()
                if (searchInput != "") {
                    viewModel.searchRecording(searchInput!!)
                }
                viewModel.cancelSearchDialog()
                KeyboardHelper.hideSoftKeyboard(editText)
            }
            setNegativeButton(context.getString(R.string.dialog_cancel_button_text)){_,_ ->
                KeyboardHelper.hideSoftKeyboard(editText)
                viewModel.cancelSearchDialog()
            }
        }
        dialog = builder.create()
        dialog.show()
        dialog.setCancelable(true)
        dialog.setOnDismissListener {
            viewModel.cancelSearchDialog()
            dialog.findViewById<EditText>(R.id.dialog_search_edit_text)?.let { editText ->
                KeyboardHelper.hideSoftKeyboard(editText)
            }
        }
    }
}