package de.ur.mi.audidroid.views


import android.app.Application
import android.os.Bundle
import android.view.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.adapter.MarkItemAdapter
import de.ur.mi.audidroid.databinding.PlayerFragmentBinding
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.HandlePlayerBar
import de.ur.mi.audidroid.viewmodels.PlayerViewModel
import kotlinx.android.synthetic.main.player_fragment.*

/**
 * The fragment allows the user to play a voice recording.
 * @author: Theresa Strohmeier
 */
class PlayerFragment : Fragment() {

    private lateinit var adapter: MarkItemAdapter
    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var args: PlayerFragmentArgs
    private lateinit var binding: PlayerFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.player_fragment, container, false)
        val application = this.activity!!.application
        val dataSource = Repository(application)

        args = PlayerFragmentArgs.fromBundle(arguments!!)
        val handlePlayerBar = initHandler()
        val viewModelFactory =
            PlayerViewModelFactory(args.recordingId, dataSource, application, handlePlayerBar)

        playerViewModel = ViewModelProvider(this, viewModelFactory).get(PlayerViewModel::class.java)

        binding.playerViewModel = playerViewModel
        binding.handlePlayerBar = handlePlayerBar
        binding.lifecycleOwner = this
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        playerViewModel.recording.observe(viewLifecycleOwner, Observer {
            it?.let {
                playerViewModel.recordingPath = it.recordingPath
                playerViewModel.initializeFrameLayout(player_layout)
                playerViewModel.initializeMediaPlayer()
                playerViewModel.initializeSeekBar(binding.seekBar)
            }
        })

        playerViewModel.allLabels.observe(viewLifecycleOwner, Observer {
            it?.let {
                for (i in it.indices) {
                    binding.labelChipGroup.addView(createChip(it[i].labelName))
                }
            }
        })
        setupAdapter()
    }

    private fun createChip(name: String): Chip {
        val chip = Chip(context)
        with(chip) {
            text = name
            isClickable = false
        }
        return chip
    }

    private fun initHandler(): HandlePlayerBar {
        return object : HandlePlayerBar {
            override fun pause() {
                playerViewModel.onPausePlayer()
            }

            override fun play() {
                playerViewModel.onStartPlayer()
            }

            override fun skipPlaying() {
                playerViewModel.skipPlaying()
            }

            override fun returnPlaying() {
                playerViewModel.returnPlaying()
            }
        }
    }

    private fun setupAdapter() {
        adapter = MarkItemAdapter(playerViewModel)
        binding.markerList.adapter = adapter

        playerViewModel.allMarks.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_player, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit_rec -> {
                navigateToEditFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToEditFragment() {
        this.findNavController().navigate(
            PlayerFragmentDirections.actionPlayerToEdit(args.recordingId)
        )
    }

    /**
     * Provides the Repository, recordingId and context to the PlayerViewModel.
     */
    class PlayerViewModelFactory(
        private val recordingId: Int,
        private val dataSource: Repository,
        private val application: Application,
        private val handlePlayerBar: HandlePlayerBar
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
                return PlayerViewModel(recordingId, dataSource, application, handlePlayerBar) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
