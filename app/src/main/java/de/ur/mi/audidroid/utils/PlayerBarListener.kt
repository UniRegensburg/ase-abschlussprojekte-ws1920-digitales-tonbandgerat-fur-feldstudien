package de.ur.mi.audidroid.utils

/**
 * The interface for the player-bar to handle the play options
 * @author: Sabine Roth
 */

interface PlayerBarListener {

    fun play() {}

    fun pause() {}

    fun skipPlaying() {}

    fun returnPlaying() {}

    fun fastForward() {}

    fun fastRewind() {}
}
