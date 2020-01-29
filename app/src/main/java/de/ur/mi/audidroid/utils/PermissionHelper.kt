package de.ur.mi.audidroid.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.ur.mi.audidroid.R

/**
 * The PermissionHelper checks if required permissions were given and creates an dialog using the Dialog object if they weren't.
 * @author: Sabine Roth
 */


object PermissionHelper {

    private const val recordPermission = Manifest.permission.RECORD_AUDIO
    private const val writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private const val readPermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private var permissionsResult: String? = null
    private val firstRequest = Resources.getSystem().getString(android.R.string.ok)

    fun permissionsGranted(context: Context): String? {

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


    fun showDialog(missingPermission: String, context: Context) {
        when (missingPermission) {
            recordPermission -> Dialog.createDialog(
                context = context,
                textId = R.string.permission_record
            )
            writePermission -> Dialog.createDialog(
                context = context,
                textId = R.string.permission_write
            )
            readPermission -> Dialog.createDialog(
                context = context,
                textId = R.string.permission_read
            )
            firstRequest -> {
                makeRequest(context)
                return
            }
        }
    }

    fun makeRequest(context: Context) {
        val permissions = arrayOf(
            recordPermission,
            writePermission,
            readPermission
        )
        ActivityCompat.requestPermissions(
            context as Activity,
            permissions,
            context.resources.getInteger(R.integer.database_request_code)
        )
    }
}
