package de.ur.mi.audidroid.utils

import android.util.Log
import com.arthenica.mobileffmpeg.Config
import java.io.File
import java.io.IOException

class AudioEditor {

    private var audio: File? = null
    private var startTime = "00:00:00"
    private var endTime = "00:00:00"
    private var duration = ""
    private var outputPath = ""
    private var outputFileName = ""
    private var callback: FFMpegCallback? = null

    fun setFile(originalFile: File) {
        this.audio = originalFile
    }

    fun setStartTime(startTime: String) {
        this.startTime = startTime
    }

    fun setEndTime(endTime: String) {
        this.endTime = endTime
    }

    fun setDuration(duration: String) {
        this.duration = duration
    }

    fun setOutputPath(output: String) {
        this.outputPath = output
    }

    fun setOutputFileName(output: String) {
        this.outputFileName = output
    }

    fun setCallback(callback: FFMpegCallback) {
        this.callback = callback
    }

    fun cut(type: String) {
        if (audio == null || !audio!!.exists()) {
            callback!!.onFailure(IOException("File not exists"))
            return
        }

        if (!audio!!.canRead()) {
            callback!!.onFailure(IOException("Can't read the file. Missing Permission?"))
            return
        }

        val outputLocation = getConvertedFile(outputPath, outputFileName)

        var cmd = arrayOf("")
        if (type == "cutInner") {
            cmd = arrayOf(
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
        } else if (type == "cutOuter") {
            val filter =
                "[0:a]atrim=start=0:end=$startTime,asetpts=PTS-STARTPTS[a]; [0:a]atrim=start=$endTime:end=$duration,asetpts=PTS-STARTPTS[b]; [a][b]concat=n=2:v=0:a=1[out]"
            cmd = arrayOf(
                "-i",
                audio!!.path,
                "-filter_complex",
                filter,
                "-map",
                "[out]",
                outputLocation.path
            )
        }

        try {
            val response: Int = com.arthenica.mobileffmpeg.FFmpeg.execute(cmd)
            if (response == Config.RETURN_CODE_SUCCESS) {
                callback!!.onSuccess(outputLocation)
            } else if (response == Config.RETURN_CODE_CANCEL) {
                Log.d("AudioEditor", "Cut recording canceled")
            } else {
                callback!!.onFailure(IOException("Cut recording failed for $cmd"))
            }
        } catch (e: Exception) {
            callback!!.onFailure(e)
        }
    }

    private fun getConvertedFile(folder: String, fileName: String): File {
        val f = File(folder)

        if (!f.exists())
            f.mkdir()

        return File(f.path + File.separator + fileName)
    }
}
