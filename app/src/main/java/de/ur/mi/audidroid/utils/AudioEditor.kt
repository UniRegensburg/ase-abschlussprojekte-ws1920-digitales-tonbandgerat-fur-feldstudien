package de.ur.mi.audidroid.utils

import android.content.Context
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import java.io.File
import java.io.IOException

class AudioEditor private constructor(private val context: Context) {

    private var audio: File? = null
    private var startTime = "00:00:00"
    private var endTime = "00:00:00"
    private var duration = ""
    private var outputPath = ""
    private var outputFileName = ""
    private var callback: FFMpegCallback? = null

    fun setFile(originalFile: File): AudioEditor {
        this.audio = originalFile
        return this
    }

    fun setStartTime(startTime: String): AudioEditor {
        this.startTime = startTime
        return this
    }

    fun setEndTime(endTime: String): AudioEditor {
        this.endTime = endTime
        return this
    }

    fun setDuration(duration: String): AudioEditor {
        this.duration = duration
        return this
    }

    fun setOutputPath(output: String): AudioEditor {
        this.outputPath = output
        return this
    }

    fun setOutputFileName(output: String): AudioEditor {
        this.outputFileName = output
        return this
    }

    fun setCallback(callback: FFMpegCallback): AudioEditor {
        this.callback = callback
        return this
    }

    fun cutInner() {
        if (audio == null || !audio!!.exists()) {
            callback!!.onFailure(IOException("File not exists"))
            return
        }

        if (!audio!!.canRead()) {
            callback!!.onFailure(IOException("Can't read the file. Missing Permission?"))
            return
        }

        val outputLocation = getConvertedFile(outputPath, outputFileName)

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

    fun cutOuter() {
        if (audio == null || !audio!!.exists()) {
            callback!!.onFailure(IOException("File not exists"))
            return
        }

        if (!audio!!.canRead()) {
            callback!!.onFailure(IOException("Can't read the file. Missing Permission?"))
            return
        }

        val outputLocation = getConvertedFile(outputPath, outputFileName)

        val filter =
            "[0:a]atrim=start=0:end=${startTime},asetpts=PTS-STARTPTS[a]; [0:a]atrim=start=${endTime}:end=${duration},asetpts=PTS-STARTPTS[b]; [a][b]concat=n=2:v=0:a=1[out]"
        val cmd = arrayOf(
            "-i",
            audio!!.path,
            "-filter_complex",
            filter,
            "-map",
            "[out]",
            outputLocation.path
        )

        try {
            FFmpeg.getInstance(context).execute(cmd, object : ExecuteBinaryResponseHandler() {
                override fun onStart() {}

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

    private fun getConvertedFile(folder: String, fileName: String): File {
        val f = File(folder)

        if (!f.exists())
            f.mkdir()

        return File(f.path + File.separator + fileName)
    }

    companion object {
        fun with(context: Context): AudioEditor {
            return AudioEditor(context)
        }
    }

}
