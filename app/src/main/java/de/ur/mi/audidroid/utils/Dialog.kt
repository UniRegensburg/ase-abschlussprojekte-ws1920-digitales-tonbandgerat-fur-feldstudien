package de.ur.mi.audidroid.utils

import android.app.AlertDialog
import android.content.Context
import android.text.Layout
import android.view.LayoutInflater
import de.ur.mi.audidroid.R

object Dialog{

    fun createDialog(context: Context, layoutId: Int? = null, textId: Int? = null){
        val builder = AlertDialog.Builder(context)
        if(textId!=null){
            val permissionHelper = PermissionHelper(context)
            builder.setTitle(R.string.permission_title)
            builder.setMessage(textId)
            builder.setPositiveButton(
                R.string.permission_button
            ) { _, _ ->
                permissionHelper.makeRequest()
            }
        }
        if(layoutId!=null){
           /* val layoutInflater = LayoutInflater.from(context)
            val dialogView = layoutInflater.inflate(layoutId, null)*/
            builder.setView(layoutId)
        }
        val dialog = builder.create()
        dialog.show()
    }
}
