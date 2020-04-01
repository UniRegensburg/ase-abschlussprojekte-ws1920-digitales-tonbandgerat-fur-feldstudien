package de.ur.mi.audidroid.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.preference.Preference
import de.ur.mi.audidroid.R

/**
 * Pathfinder creates the dialog for the user to navigate through the folders on the device
 * and realizes the actual path from a tree-uri (This part was adapted from: https://gist.github.com/asifmujteba/d89ba9074bc941de1eaa#file-asfurihelper)
 * @author: Sabine Roth
 */

object Pathfinder {

    private lateinit var context: Context
    var preference: Preference? = null

    fun openPathDialog(preference: Preference? = null, context: Context) {
        this.context = context
        this.preference = preference
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        (context as Activity).startActivityForResult(
            intent,
            context.resources.getInteger(R.integer.activity_request_code_preference_storage)
        )
    }

    fun getRealPath(context: Context, treePath: Uri): String? {
        val docUri = DocumentsContract.buildDocumentUriUsingTree(
            treePath,
            DocumentsContract.getTreeDocumentId(treePath)
        )
        return getPath(context, docUri)
    }

    private fun getPath(context: Context, uri: Uri): String? {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                return if ("primary".equals(type, ignoreCase = true)) {
                    try {
                        Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    } catch (e: ArrayIndexOutOfBoundsException) {
                        Environment.getExternalStorageDirectory().toString() + "/"
                    }
                }
                // External SD-card
                else {
                    return null
                }
            }
        } else if ("content".equals(
                uri.scheme,
                ignoreCase = true
            )
        ) {
            return getDataColumn(
                context,
                uri
            )
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, null, null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }
}
