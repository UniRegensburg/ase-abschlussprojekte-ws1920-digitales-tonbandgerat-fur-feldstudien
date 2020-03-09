package de.ur.mi.audidroid.utils

import de.ur.mi.audidroid.viewmodels.EditRecordingViewModel
import de.ur.mi.audidroid.viewmodels.PlayerViewModel
import de.ur.mi.audidroid.viewmodels.RecordViewModel

object HandlePlayerBar {

    fun playOnPlayerView(playerViewModel: PlayerViewModel) {
        playerViewModel.onStartPlayer()
    }

    fun pauseOnPlayerView(playerViewModel: PlayerViewModel) {
        playerViewModel.onPausePlayer()
    }

    fun playOnEditView(editViewModel: EditRecordingViewModel) {
        editViewModel.onStartPlayer()
    }

    fun pauseOnEditView(editViewModel: EditRecordingViewModel) {
        editViewModel.onPausePlayer()
    }
}
