package de.ur.mi.audidroid.utils

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import de.ur.mi.audidroid.R

/**
 * [HandlePlayerBar] executes the actions of the player-bar
 * @author: Sabine Roth
 */

object HandlePlayerBar {

    fun skipPlaying(mediaPlayer: MediaPlayer, context: Context) {
        val moveTime =
            mediaPlayer.currentPosition + context.resources.getInteger(R.integer.jump_amount)
                .toLong()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) mediaPlayer.seekTo(
            moveTime,
            MediaPlayer.SEEK_NEXT_SYNC
        ) else mediaPlayer.seekTo(moveTime.toInt())
    }

    fun returnPlaying(mediaPlayer: MediaPlayer, context: Context) {
        val moveTime =
            mediaPlayer.currentPosition - context.resources.getInteger(R.integer.jump_amount)
                .toLong()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) mediaPlayer.seekTo(
            moveTime,
            MediaPlayer.SEEK_PREVIOUS_SYNC
        ) else mediaPlayer.seekTo(moveTime.toInt())
    }

    fun fastForward(mediaPlayer: MediaPlayer) {
        if (mediaPlayer.isPlaying) {
            val newParams = mediaPlayer.playbackParams
            newParams.speed = mediaPlayer.playbackParams.speed + 0.25f
            mediaPlayer.playbackParams = newParams
        }
    }

    fun fastRewind(mediaPlayer: MediaPlayer) {
        if (mediaPlayer.isPlaying && mediaPlayer.playbackParams.speed > 0.25f) {
            val newParams = mediaPlayer.playbackParams
            newParams.speed = mediaPlayer.playbackParams.speed - 0.25f
            mediaPlayer.playbackParams = newParams
        }
    }
}
