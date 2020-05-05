package de.ur.mi.audidroid.views

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MenuInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.adapter.EditMarkerItemAdapter
import de.ur.mi.audidroid.adapter.MarkerButtonEditRecAdapter
import de.ur.mi.audidroid.databinding.EditRecordingFragmentBinding
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.PlayerBarListener
import de.ur.mi.audidroid.viewmodels.EditRecordingViewModel
import kotlinx.android.synthetic.main.edit_recording_fragment.marker_button_list1
import kotlinx.android.synthetic.main.player_fragment.player_layout

/**
 * The EditRecordingFragment displays the view to edit a recording.
 * @author: Theresa Strohmeier
 */
class EditRecordingFragment : Fragment() {

    private lateinit var editMarksAdapter: EditMarkerItemAdapter
    private lateinit var markerButtonAdapter: MarkerButtonEditRecAdapter
    private lateinit var editRecordingViewModel: EditRecordingViewModel
    private lateinit var binding: EditRecordingFragmentBinding
    private lateinit var dataSource: Repository
    private lateinit var args: EditRecordingFragmentArgs

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.edit_recording_fragment, container, false)

        val application = this.requireActivity().application
        args = EditRecordingFragmentArgs.fromBundle(requireArguments())

        dataSource = Repository(application)
        val viewModelFactory =
            EditViewModelFactory(args.recordingId, dataSource, application)

        editRecordingViewModel =
            ViewModelProvider(this, viewModelFactory).get(EditRecordingViewModel::class.java)

        binding.editRecordingViewModel = editRecordingViewModel

        binding.playerBarListener = initPlayerBarListener()

        binding.lifecycleOwner = this
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        editRecordingViewModel.recording.observe(viewLifecycleOwner, Observer {
            it?.let {
                editRecordingViewModel.tempFile = it.recordingPath
                editRecordingViewModel.initializeLayout(player_layout)
                editRecordingViewModel.initializeMediaPlayer()
                editRecordingViewModel.initializeSeekBar(binding.seekBar)
                editRecordingViewModel.initializeRangeBar(binding.rangeBar)
            }
        })

        onBackButtonPressed()
        navigateToPreviousFragment()
        navigateToFilesFragment()
        createEditRecordingDialog()
        createCommentDialog()
        createConfirmDialog()
        createCancelEditingDialog()
        setupEditMarksAdapter()
        setupMarkerButtonAdapter()
    }

    private fun initPlayerBarListener(): PlayerBarListener {
        return object : PlayerBarListener {
            override fun pause() {
                editRecordingViewModel.onPausePlayer()
            }

            override fun play() {
                editRecordingViewModel.onStartPlayer()
            }

            override fun skipPlaying() {
                editRecordingViewModel.skipPlaying()
            }

            override fun returnPlaying() {
                editRecordingViewModel.returnPlaying()
            }

            override fun fastForward() {
                editRecordingViewModel.fastForward()
            }

            override fun fastRewind() {
                editRecordingViewModel.fastRewind()
            }
        }
    }

    private fun onBackButtonPressed() {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    editRecordingViewModel.onBackPressed()
                }
            })
    }

    private fun navigateToPreviousFragment() {
        editRecordingViewModel.navigateToPreviousFragment.observe(viewLifecycleOwner, Observer {
            if (it) {
                findNavController().popBackStack()
            }
        })
    }

    private fun navigateToFilesFragment() {
        editRecordingViewModel.navigateToFilesFragment.observe(viewLifecycleOwner, Observer {
            if (it) {
                this.findNavController().navigate(
                    EditRecordingFragmentDirections.actionEditToFiles()
                )
                editRecordingViewModel.onFilesFragmentNavigated()
            }
        })
    }

    private fun createEditRecordingDialog() {
        editRecordingViewModel.createSaveDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                de.ur.mi.audidroid.utils.EditRecordingDialog.createDialog(
                    paramContext = requireContext(),
                    layoutId = R.layout.save_dialog,
                    recordingId = args.recordingId,
                    recordingName = args.recordingName,
                    recordingPath = args.recordingPath,
                    viewModel = editRecordingViewModel,
                    errorMessage = editRecordingViewModel.saveErrorMessage,
                    editRecordingFragment = this,
                    dataSource = dataSource
                )
            }
        })
    }

    private fun createCommentDialog() {
        editRecordingViewModel.createCommentDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                de.ur.mi.audidroid.utils.CommentDialog.createDialog(
                    context = requireContext(),
                    markTimestampToBeEdited = editRecordingViewModel.markTimestampToBeEdited,
                    layoutId = R.layout.comment_dialog,
                    viewModel = editRecordingViewModel,
                    errorMessage = editRecordingViewModel.commentErrorMessage
                )
            }
        })
    }

    private fun createConfirmDialog() {
        editRecordingViewModel.createConfirmDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                de.ur.mi.audidroid.utils.DeleteMarkDialog.createDialog(
                    context = requireContext(),
                    markToBeEdited = editRecordingViewModel.markToBeDeleted,
                    viewModel = editRecordingViewModel
                )
            }
        })
    }

    private fun createCancelEditingDialog() {
        editRecordingViewModel.createCancelEditingDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                de.ur.mi.audidroid.utils.CancelEditingDialog.createDialog(
                    context = requireContext(),
                    viewModel = editRecordingViewModel
                )
            }
        })
    }

    private fun setupEditMarksAdapter() {
        editMarksAdapter = EditMarkerItemAdapter(editRecordingViewModel)
        binding.markerList.adapter = editMarksAdapter

        editRecordingViewModel.copyMarks()

        editRecordingViewModel.allMarks.observe(viewLifecycleOwner, Observer {
            it?.let {
                editMarksAdapter.submitList(it)
            }
        })
    }

    private fun setupMarkerButtonAdapter() {
        markerButtonAdapter = MarkerButtonEditRecAdapter(editRecordingViewModel)
        binding.markerButtonList1.adapter = markerButtonAdapter

        val layoutManager = GridLayoutManager(context, 3)
        marker_button_list1.layoutManager = layoutManager

        editRecordingViewModel.allMarkers.observe(viewLifecycleOwner, Observer {
            it?.let {
                markerButtonAdapter.submitList(it)
                layoutManager.spanCount = getSpanCount(it.size)
            }
        })
    }

    private fun getSpanCount(numberOfItems: Int): Int {
        return when (numberOfItems) {
            1 -> 1
            2 -> 2
            else -> 3
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_edit_recording, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save_edited_rec -> {
                editRecordingViewModel.saveRecording()
                true
            }
            android.R.id.home -> {
                requireActivity().onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Provides the Repository, recordingPath and context to the EditRecordingViewModel.
     */
    class EditViewModelFactory(
        private val recordingId: Int,
        private val dataSource: Repository,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditRecordingViewModel::class.java)) {
                return EditRecordingViewModel(
                    recordingId,
                    dataSource,
                    application
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
