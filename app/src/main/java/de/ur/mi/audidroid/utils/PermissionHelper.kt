package de.ur.mi.audidroid.utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.ur.mi.audidroid.R

class PermissionHelper(val context: Context) {

    private val recordPermission = Manifest.permission.RECORD_AUDIO
    private val writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val readPermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private val requestCode =  context.resources.getInteger(R.integer.database_request_code)
    private var permissionsResult: String? = null
    private val firstRequest = Resources.getSystem().getString(android.R.string.ok)


    fun permissionsGranted(): String? {
        if (ContextCompat.checkSelfPermission(
                context,
                recordPermission
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                context,
                writePermission
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                context,
                readPermission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            /** Permission/s not granted */
            when {
                /** Permission have been denied */
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    recordPermission
                ) -> {
                    permissionsResult = recordPermission
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    writePermission
                ) -> {
                    permissionsResult = writePermission
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    readPermission
                ) -> {
                    permissionsResult = readPermission
                }
                /** Not asked yet */
                else -> {
                    permissionsResult = firstRequest
                }
            }
        }
        return permissionsResult
    }

    fun showDialog(missingPermission: String) {
        when (missingPermission) {
            recordPermission -> Dialog.createDialog(context = context, textId = R.string.permission_record)
            writePermission -> Dialog.createDialog(context = context, textId = R.string.permission_write)
            readPermission -> Dialog.createDialog(context = context, textId = R.string.permission_read)
            firstRequest -> {
                makeRequest()
                return
            }
        }
    }

    fun makeRequest() {
        val permissions = arrayOf(
            recordPermission,
            writePermission,
            readPermission
        )
        ActivityCompat.requestPermissions(context as Activity, permissions, requestCode)
    }
}
