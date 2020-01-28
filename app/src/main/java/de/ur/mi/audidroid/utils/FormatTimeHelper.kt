package de.ur.mi.audidroid.utils

/**
 * The object formats the time of a recording given in milliseconds to a Sting in the format Hours:Minutes:Seconds
 * @author: Sabine Roth
 */

object FormatTimeHelper {
    fun formatMilliseconds(milliseconds: Long): String? {
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
        val hoursString = if (hours > 0) {
            "$hours:"
        } else {
            ""
        }
        val secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        val minutesString = if (minutes < 10) {
            "0$minutes"
        } else {
            "" + minutes
        }
        return "$hoursString$minutesString:$secondsString"
    }
}
