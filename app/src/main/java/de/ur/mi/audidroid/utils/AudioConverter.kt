package de.ur.mi.audidroid.utils

import android.util.Log
import cafe.adriel.androidaudioconverter.callback.IConvertCallback
import cafe.adriel.androidaudioconverter.model.AudioFormat
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import java.io.File
import java.io.IOException

class AudioConverter() {

    private var audioFile: File? = null
    private var audioFormat: AudioFormat? = null
    private var callback: IConvertCallback? = null

    fun setFile(originalFile: File) {
        audioFile = originalFile
    }

    fun setFormat(format: AudioFormat) {
        audioFormat = format
    }

    fun setCallback(cb: IConvertCallback) {
        callback = cb
    }

    fun convert() {
        if (audioFile == null || !audioFile!!.exists()) {
            Log.e("AudioConverter", "File does not exist")
        }
        if (!audioFile!!.canRead()) {
            Log.e("AudioConverter", "Cannot read file. Missing permission")
        }
        val convertedFile = getConvertedFile(audioFile!!, audioFormat!!)
        val cmd: String = "-y -i ${audioFile!!.path} ${convertedFile.path}"
        try {
            val response: Int = FFmpeg.execute(cmd)
            if (response == Config.RETURN_CODE_SUCCESS) {
                callback!!.onSuccess(convertedFile)
            } else if (response == Config.RETURN_CODE_CANCEL) {
                Log.d("AudioConverter", "Conversion cancelled")
            } else {
                callback!!.onFailure(IOException("Conversion failed for $cmd"))
            }
        } catch (e: Exception) {
            callback!!.onFailure(e)
        }
    }

    private fun getConvertedFile(orgFile: File, convertFormat: AudioFormat): File {
        val f: List<String> = orgFile.absolutePath.split(".")
        val filePath: String = orgFile.absolutePath.replace(f[f.size - 1], convertFormat.format)
        return File(filePath)
    }

}
