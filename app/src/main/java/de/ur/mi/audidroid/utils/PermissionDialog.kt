package de.ur.mi.audidroid.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.ur.mi.audidroid.R

/**
 * [PermissionDialog] creates dialogs for missing or denied permissions
 * @author: Sabine Roth
 */

object PermissionDialog {

    fun createDialog(context: Context, textId: Int) {
        val dialog: androidx.appcompat.app.AlertDialog
        val builder = MaterialAlertDialogBuilder(context)
        with(builder) {
            setTitle(R.string.permission_title)
            setMessage(textId)
            setPositiveButton(
                R.string.permission_button
            ) { _, _ ->
                PermissionHelper.makeRequest(context)
            }
        }
        dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
    }
}
