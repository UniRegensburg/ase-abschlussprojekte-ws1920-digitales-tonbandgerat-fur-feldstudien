package de.ur.mi.audidroid.viewmodels

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import de.ur.mi.audidroid.R


class RecordViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    private var isRecording = false
    private val mediaRecorder: MediaRecorder = MediaRecorder()


    fun initializeRecorder(context: Context){
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(context as Activity, permissions,0)
        }

        val output = context.getFilesDir().getAbsolutePath() + "/" + "recording" + ".aac" //TODO ändern
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setOutputFile(output)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder.prepare()
    }

    fun recordButtonClicked(button: ImageButton) =
        when(!isRecording) {
            true -> {
                button.setImageResource(R.mipmap.pause_button_foreground)
                isRecording = true
                startRecording()
            }

            false ->{
                button.setImageResource(R.mipmap.record_button_foreground)
                isRecording = false
                pauseRecording()
            }
    }

    private fun startRecording(){
        mediaRecorder.start()
    }

    private fun pauseRecording(){
        mediaRecorder.stop()
    }

    fun stopRecording(){
        mediaRecorder.release()
    }
}
