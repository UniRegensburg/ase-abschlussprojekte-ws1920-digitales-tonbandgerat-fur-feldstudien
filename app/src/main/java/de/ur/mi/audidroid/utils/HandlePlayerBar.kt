package de.ur.mi.audidroid.utils

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import de.ur.mi.audidroid.R

interface HandlePlayerBar {

    fun play() {}

    fun pause() {}

    fun skipPlaying() {}

    fun returnPlaying() {}

    fun doSkippingPlaying(mediaPlayer: MediaPlayer, context: Context){
        val moveTime =
            mediaPlayer.currentPosition + context.resources.getInteger(R.integer.jump_amount).toLong()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) mediaPlayer.seekTo(
            moveTime,
            MediaPlayer.SEEK_NEXT_SYNC
        ) else mediaPlayer.seekTo(moveTime.toInt())
    }

    fun doReturnPlaying(mediaPlayer: MediaPlayer, context: Context){
        val moveTime =
            mediaPlayer.currentPosition - context.resources.getInteger(R.integer.jump_amount).toLong()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) mediaPlayer.seekTo(
            moveTime,
            MediaPlayer.SEEK_PREVIOUS_SYNC
        ) else mediaPlayer.seekTo(moveTime.toInt())
    }
}
