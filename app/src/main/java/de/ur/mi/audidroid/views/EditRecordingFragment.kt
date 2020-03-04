package de.ur.mi.audidroid.views

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.databinding.EditRecordingFragmentBinding
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.viewmodels.EditRecordingViewModel
import kotlinx.android.synthetic.main.player_fragment.*

class EditRecordingFragment : Fragment() {

    private lateinit var editRecordingViewModel: EditRecordingViewModel
    private lateinit var binding: EditRecordingFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.edit_recording_fragment, container, false)

        val application = this.activity!!.application
        val args = EditRecordingFragmentArgs.fromBundle(arguments!!)

        val dataSource = Repository(application)
        val viewModelFactory = EditViewModelFactory(args.recordingPath, dataSource, application)

        editRecordingViewModel =
            ViewModelProvider(this, viewModelFactory).get(EditRecordingViewModel::class.java)

        binding.editRecordingViewModel = editRecordingViewModel

        binding.setLifecycleOwner(this)
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        editRecordingViewModel.initializeMediaPlayer()
        editRecordingViewModel.initializeSeekBar(binding.seekBar)
        editRecordingViewModel.initializeFrameLayout(player_layout)
        editRecordingViewModel.initializeRangeBar(binding.rangeBar)
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
        private val recordingPath: String,
        private val dataSource: Repository,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditRecordingViewModel::class.java)) {
                return EditRecordingViewModel(recordingPath, dataSource, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
