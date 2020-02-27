package de.ur.mi.audidroid.views


import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.ur.mi.audidroid.databinding.PlayerFragmentBinding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.viewmodels.PlayerViewModel
import kotlinx.android.synthetic.main.player_fragment.*
import java.lang.IllegalArgumentException

/**
 * The fragment allows the user to play a voice recording.
 * @author: Theresa Strohmeier
 */
class PlayerFragment : Fragment() {

    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var binding: PlayerFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.player_fragment, container, false)
        val application = this.activity!!.application
        val dataSource = Repository(application)

        val args = PlayerFragmentArgs.fromBundle(arguments!!)
        val viewModelFactory = PlayerViewModelFactory(dataSource, args.recordingId, application)
        playerViewModel = ViewModelProvider(this, viewModelFactory).get(PlayerViewModel::class.java)
        binding.playerViewModel = playerViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        playerViewModel.initializeFrameLayout(player_layout)
        playerViewModel.recording.observe(viewLifecycleOwner, Observer {
            it?.let {
                playerViewModel.initializeMediaPlayer(it.recordingPath)
                playerViewModel.initializeSeekBar(binding.seekBar)
            }
        })
    }

    /**
     * Provides the Repository, recordingId and context to the PlayerViewModel.
     */
    class PlayerViewModelFactory(
        private val dataSource: Repository,
        private val recordingId: Int,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
                return PlayerViewModel(dataSource, recordingId, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
