package de.ur.mi.audidroid.utils

import de.ur.mi.audidroid.viewmodels.PlayerViewModel
import de.ur.mi.audidroid.viewmodels.RecordViewModel

object HandlePlayerBar {

    fun playOnPlayerView(playerViewModel: PlayerViewModel) {
        playerViewModel.onStartPlayer()
        println("playPlayer")
    }

    fun pauseOnPlayerView(playerViewModel: PlayerViewModel) {
        playerViewModel.onPausePlayer()
        println("pausePlayer")
    }

    fun playOnEditView(recordViewModel: RecordViewModel) {
        println("playEdit")
    }

    fun pauseOnEditView(recordViewModel: RecordViewModel) {
        println("pauseEdit")
    }
}
