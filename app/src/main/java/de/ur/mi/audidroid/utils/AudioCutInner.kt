package de.ur.mi.audidroid.utils

import android.content.Context
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import java.io.File
import java.io.IOException

class AudioCutInner private constructor(private val context: Context) {

    private var audio: File? = null
    private var startTime = "00:00:00"
    private var endTime = "00:00:00"
    private var outputPath = ""
    private var outputFileName = ""
    private var callback: FFMpegCallback? = null

    fun setFile(originalFile: File): AudioCutInner {
        this.audio = originalFile
        return this
    }

    fun setStartTime(startTime: String): AudioCutInner {
        this.startTime = startTime
        return this
    }

    fun setEndTime(endTime: String): AudioCutInner {
        this.endTime = endTime
        return this
    }

    fun setOutputPath(output: String): AudioCutInner {
        this.outputPath = output
        return this
    }

    fun setOutputFileName(output: String): AudioCutInner {
        this.outputFileName = output
        return this
    }

    fun setCallback(callback: FFMpegCallback): AudioCutInner {
        this.callback = callback
        return this
    }

    fun trim() {
        if (audio == null || !audio!!.exists()) {
            callback!!.onFailure(IOException("File not exists"))
            return
        }

        if (!audio!!.canRead()) {
            callback!!.onFailure(IOException("Can't read the file. Missing Permission?"))
            return
        }

        val outputLocation = Utils.getConvertedFile(outputPath, outputFileName)

        val cmd = arrayOf(
            "-i",
            audio!!.path,
            "-ss",
            startTime,
            "-to",
            endTime,
            "-c",
            "copy",
            outputLocation.path
        )

        try {
            FFmpeg.getInstance(context).execute(cmd, object : ExecuteBinaryResponseHandler() {
                override fun onSuccess(message: String?) {
                    callback!!.onSuccess(outputLocation, "audio")
                }

                override fun onFailure(message: String?) {
                    if (outputLocation.exists()) {
                        outputLocation.delete()
                    }
                    callback!!.onFailure(IOException(message))
                }

                override fun onFinish() {
                    callback!!.onFinish()
                }
            })

        } catch (e: Exception) {
            callback!!.onFailure(e)
        } catch (e2: FFmpegCommandAlreadyRunningException) {
            callback!!.onNotAvailable(e2)
        }

    }

    companion object {
        fun with(context: Context): AudioCutInner {
            return AudioCutInner(context)
        }
    }

}
