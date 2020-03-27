package de.ur.mi.audidroid.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.ceil


class SoundBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet
) : View(context, attrs) {
    /**
     * bytes array converted from file.
     */
    private var bytes: ByteArray? = null
    /**
     * Percentage of audio sample scale
     * Should updated dynamically while audioPlayer is played
     */
    private var denseness = 0f
    /**
     * Canvas painting for sample scale, filling played part of audio sample
     */
    private val playedStatePainting: Paint = Paint()
    /**
     * Canvas painting for sample scale, filling not played part of audio sample
     */
    private val notPlayedStatePainting: Paint = Paint()


    private var canvasWidth = 0
    private var canvasHeight = 0


    init {
        bytes = null
        playedStatePainting.strokeWidth = 1f
        playedStatePainting.isAntiAlias = true
        playedStatePainting.color =
            ContextCompat.getColor(context, de.ur.mi.audidroid.R.color.color_primary)
        notPlayedStatePainting.strokeWidth = 1f
        notPlayedStatePainting.isAntiAlias = true
        notPlayedStatePainting.color =
            ContextCompat.getColor(context, de.ur.mi.audidroid.R.color.grayed_out)
    }


    /**
     * update and redraw Visualizer view
     */
    fun updateVisualizer(uri: Uri) {
        val temp = uri.authority
        val inputStream = context.contentResolver.openInputStream(uri)!!
        bytes = readInputStream(inputStream)
        invalidate()
    }

    fun updateVisualizer(byteArray: ByteArray) {
        bytes = byteArray
        invalidate()
    }


    private fun readInputStream(inputStream: InputStream): ByteArray? {
        val outputStream = ByteArrayOutputStream()
        val buf = ByteArray(1024)
        var len: Int
        try {
            while (inputStream.read(buf).also { len = it } != -1) {
                outputStream.write(buf, 0, len)
            }
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
        }
        return outputStream.toByteArray()
    }

    /**
     * Update player percent. 0 - file not played, 1 - full played
     *
     * @param percent
     */
    fun updatePlayerPercent(percent: Float) {
        val temp: Double = width * percent.toDouble()
        denseness = ceil(temp).toFloat()
        if (denseness < 0) {
            denseness = 0f
        } else if (denseness > width) {
            denseness = width.toFloat()
        }
        invalidate()
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)
        canvasWidth = measuredWidth
        canvasHeight = measuredHeight
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bytes == null || width == 0) {
            return
        }
        val totalBarsCount = width / dp(3f)
        if (totalBarsCount <= 0.1f) {
            return
        }
        var value: Byte
        val samplesCount : Int = bytes!!.size * 8 / 5
        val samplesPerBar : Float = (samplesCount / totalBarsCount).toFloat()
        var barCounter: Float  = 0f
        var nextBarNum : Int = 0
        val y: Int = (height - dp(VISUALIZER_HEIGHT.toFloat())) / 2
        var barNum : Int = 0
        var lastBarNum: Int
        var drawBarCount: Int
        for (a in 0 until samplesCount) {
            if (a != nextBarNum) {
                continue
            }
            drawBarCount = 0
            lastBarNum = nextBarNum
            while (lastBarNum == nextBarNum) {
                barCounter += samplesPerBar
                nextBarNum = barCounter.toInt()
                drawBarCount++
            }
            val bitPointer: Int = a * 5
            val byteNum : Int = bitPointer / java.lang.Byte.SIZE
            val byteBitOffset: Int = bitPointer - byteNum * java.lang.Byte.SIZE
            val currentByteCount : Int = java.lang.Byte.SIZE - byteBitOffset
            val nextByteRest: Int = 5 - currentByteCount
            val tempbyte : Byte = bytes!![byteNum]
            val first = (tempbyte.toInt() shr byteBitOffset).toByte()
            val second = (2 shl (Math.min(5,currentByteCount) - 1))-1
            val temp = first and second.toByte()
            value = temp
            if (nextByteRest > 0) {
                value = (value.toInt() shl nextByteRest).toByte()
                value = value or bytes!![byteNum + 1] and ((2 shl nextByteRest - 1) - 1).toByte()
            }
            for (b in 0 until drawBarCount) {
                val left: Float = barNum * dp(4f).toFloat()
                val top: Float = y + dp(
                    VISUALIZER_HEIGHT - Math.max(
                        1f,
                        VISUALIZER_HEIGHT * value / 31.0f
                    )
                ).toFloat()
                val right: Float = left + dp(3f)
                val bottom: Float =
                    y + dp(VISUALIZER_HEIGHT.toFloat()).toFloat()
                if (left < denseness && left + dp(2f) < denseness) {
                    canvas.drawRect(left, top, right, bottom, playedStatePainting)
                } else {
                    canvas.drawRect(left, top, right, bottom, notPlayedStatePainting)
                    if (left < denseness) {
                        canvas.drawRect(left, top, right, bottom, playedStatePainting)
                    }
                }
                barNum++
            }
        }
    }

    private fun dp(value: Float): Int {
        return if (value == 0f) {
            0
        } else {
            val temp = (context.resources.displayMetrics.density * value).toDouble()
            ceil(temp).toInt()
        }
    }

    companion object {
        /**
         * constant value for Height of the bar
         */
        const val VISUALIZER_HEIGHT = 28
    }
}
