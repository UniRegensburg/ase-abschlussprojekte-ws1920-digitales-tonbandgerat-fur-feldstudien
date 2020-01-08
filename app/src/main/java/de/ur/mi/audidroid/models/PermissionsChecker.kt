package de.ur.mi.audidroid.models

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.viewmodels.RecordViewModel

class PermissionsChecker(val context: Context) : AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback {

    private val recordPermission = Manifest.permission.RECORD_AUDIO
    private val writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val readPermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private val requestCode = 101
    private var allPermissionsGranted = false
    private var resultReady = false
    private var recordModel: RecordViewModel = RecordViewModel()


    fun checkNeededPermissions(){
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
                /** Permission/s have been denied */
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    recordPermission
                ) -> {
                    showDialog(recordPermission)
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    writePermission
                ) -> {
                    showDialog(writePermission)
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    readPermission
                ) -> {
                    showDialog(readPermission)
                }
                /** Not asked yet */
                else -> {
                    makeRequest()
                }
            }
          /*  synchronized(this){
                while(!resultReady){
                    print("sth")
                    //wait for result
                }
            }*/
        }/* else {
            allPermissionsGranted = true
        }*/
       // return allPermissionsGranted
    }

    private fun showDialog(missingPermission: String) {
        val builder = AlertDialog.Builder(context)
        when (missingPermission) {
            recordPermission -> builder.setMessage(R.string.permission_record)
            writePermission -> builder.setMessage(R.string.permission_write)
            readPermission -> builder.setMessage(R.string.permission_read)
        }
        builder.setTitle(R.string.permission_title)
        builder.setPositiveButton(
            R.string.permission_button
        ) { _, _ ->
            makeRequest()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun makeRequest() {
        val permissions = arrayOf(
            recordPermission,
            writePermission,
            readPermission
        )
        ActivityCompat.requestPermissions(context as Activity, permissions, requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            this.requestCode -> {
                when (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                        grantResults[1] != PackageManager.PERMISSION_GRANTED || grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                    false -> recordModel.initializeRecorder(context)
                }
               /* allPermissionsGranted =
                    when (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                            grantResults[1] != PackageManager.PERMISSION_GRANTED || grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                        true -> RecordViewModel.initialize
                        false -> true
                    }*/
            }
        }
       // resultReady = true
    }
}
