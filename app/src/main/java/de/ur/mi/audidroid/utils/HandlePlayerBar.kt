package de.ur.mi.audidroid.utils

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import de.ur.mi.audidroid.R

/**
 * [HandlePlayerBar] executes the actions of the player-bar
 * @author: Sabine Roth, Jonas Puchinger
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

    fun fastForward(
        mediaPlayer: MediaPlayer,
        context: Context,
        buttonForward: ImageButton,
        buttonRewind: ImageButton
    ) {
        if (!mediaPlayer.isPlaying) return
        if (mediaPlayer.playbackParams.speed < 2.5f) {
            val newParams = mediaPlayer.playbackParams
            newParams.speed = mediaPlayer.playbackParams.speed + 0.25f
            mediaPlayer.playbackParams = newParams
        } else disableButton(buttonForward, context)
        enableButton(buttonRewind, context)
    }

    fun fastRewind(
        mediaPlayer: MediaPlayer,
        context: Context,
        buttonRewind: ImageButton,
        buttonForward: ImageButton
    ) {
        if (!mediaPlayer.isPlaying) return
        if (mediaPlayer.playbackParams.speed > 0.25f) {
            val newParams = mediaPlayer.playbackParams
            newParams.speed = mediaPlayer.playbackParams.speed - 0.25f
            mediaPlayer.playbackParams = newParams
        } else disableButton(buttonRewind, context)
        enableButton(buttonForward, context)
    }

    private fun disableButton(button: ImageButton, context: Context) {
        button.backgroundTintList = ContextCompat.getColorStateList(context, R.color.grayed_out)
        button.isEnabled = false
    }

    private fun enableButton(button: ImageButton, context: Context) {
        if (!button.isEnabled) {
            button.backgroundTintList =
                ContextCompat.getColorStateList(context, ColorHelper.getThemedIconColor())
            button.isEnabled = true
        }
    }
}
