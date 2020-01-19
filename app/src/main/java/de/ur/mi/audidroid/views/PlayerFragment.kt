package de.ur.mi.audidroid.views


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import de.ur.mi.audidroid.databinding.PlayerFragmentBinding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.viewmodels.PlayerViewModel
import java.lang.IllegalArgumentException

/**
 * ViewModel for PlayerFragment.
 * @author
 */
class PlayerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: PlayerFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.player_fragment, container, false)

        val application = requireNotNull(this.activity).application

        val args = PlayerFragmentArgs.fromBundle(arguments!!)
        Toast.makeText(context, "RecordingId: ${args.recordingPath}", Toast.LENGTH_SHORT).show()

        val dataSource = Repository(application)
        val viewModelFactory = PlayerViewModelFactory(args.recordingPath, dataSource)

        val playerViewModel =
            ViewModelProviders.of(this, viewModelFactory).get(PlayerViewModel::class.java)

        binding.playerViewModel = playerViewModel

        binding.setLifecycleOwner(this)

        return binding.root
    }

    /**
     * Provides variable and context to the ViewModel.
     */
    class PlayerViewModelFactory(
        private val recordingPath: String,
        private val dataSource: Repository
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
                return PlayerViewModel(recordingPath, dataSource) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
