package de.ur.mi.audidroid.views

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.adapter.EditMarkerItemAdapter
import de.ur.mi.audidroid.databinding.EditRecordingFragmentBinding
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.HandlePlayerBar
import de.ur.mi.audidroid.viewmodels.EditRecordingViewModel
import kotlinx.android.synthetic.main.player_fragment.*

class EditRecordingFragment : Fragment() {

    private lateinit var adapter: EditMarkerItemAdapter
    private lateinit var editRecordingViewModel: EditRecordingViewModel
    private lateinit var binding: EditRecordingFragmentBinding
    private lateinit var dataSource: Repository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.edit_recording_fragment, container, false)

        val application = this.activity!!.application
        val args = EditRecordingFragmentArgs.fromBundle(arguments!!)

        dataSource = Repository(application)
        val handlePlayerBar = initHandler()
        val viewModelFactory =
            EditViewModelFactory(args.recordingId, dataSource, application, handlePlayerBar)

        editRecordingViewModel =
            ViewModelProvider(this, viewModelFactory).get(EditRecordingViewModel::class.java)

        binding.editRecordingViewModel = editRecordingViewModel

        binding.handlePlayerBar = handlePlayerBar

        binding.lifecycleOwner = this
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        editRecordingViewModel.recording.observe(viewLifecycleOwner, Observer {
            it?.let {
                editRecordingViewModel.tempFile = it.recordingPath
                editRecordingViewModel.initializeMediaPlayer()
                editRecordingViewModel.initializeSeekBar(binding.seekBar)
                editRecordingViewModel.initializeFrameLayout(player_layout)
                editRecordingViewModel.initializeRangeBar(binding.rangeBar)
            }
        })
        createEditRecordingDialog()
        createCommentDialog()
        createConfirmDialog()
        setupAdapter()
    }

    private fun initHandler(): HandlePlayerBar {
        return object : HandlePlayerBar {
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
        }
    }

    private fun createEditRecordingDialog() {
        editRecordingViewModel.createSaveDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                de.ur.mi.audidroid.utils.EditRecordingDialog.createDialog(
                    paramContext = context!!,
                    layoutId = R.layout.save_dialog,
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
                    context = context!!,
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
                    context = context!!,
                    markToBeEdited = editRecordingViewModel.markToBeDeleted,
                    viewModel = editRecordingViewModel
                )
            }
        })
    }

    private fun setupAdapter() {
        adapter = EditMarkerItemAdapter(editRecordingViewModel)
        binding.markerList.adapter = adapter

        editRecordingViewModel.allMarks.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Provides the Repository, recordingPath and context to the EditRecordingViewModel.
     */
    class EditViewModelFactory(
        private val recordingId: Int,
        private val dataSource: Repository,
        private val application: Application,
        private val handlePlayerBar: HandlePlayerBar
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditRecordingViewModel::class.java)) {
                return EditRecordingViewModel(
                    recordingId,
                    dataSource,
                    application,
                    handlePlayerBar
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
