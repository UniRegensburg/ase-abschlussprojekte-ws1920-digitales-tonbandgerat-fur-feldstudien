package de.ur.mi.audidroid.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.preference.Preference
import de.ur.mi.audidroid.R

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
}
