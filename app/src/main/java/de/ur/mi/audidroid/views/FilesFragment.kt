package de.ur.mi.audidroid.views

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.adapter.RecordingItemAdapter
import de.ur.mi.audidroid.databinding.FilesFragmentBinding
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.viewmodels.FilesViewModel
import kotlinx.android.synthetic.main.files_fragment.*

/**
 * The fragment displays all recordings.
 * @author: Theresa Strohmeier
 */
class FilesFragment : Fragment() {

    private lateinit var adapter: RecordingItemAdapter
    private lateinit var binding: FilesFragmentBinding
    private lateinit var filesViewModel: FilesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.files_fragment, container, false)
        val application = this.activity!!.application
        val dataSource = Repository(application)

        val viewModelFactory = FilesViewModelFactory(dataSource, application)
        filesViewModel = ViewModelProvider(this, viewModelFactory).get(FilesViewModel::class.java)
        binding.filesViewModel = filesViewModel
        binding.lifecycleOwner = this

        //Observer on the state variable for showing Snackbar message when a list-item is deleted.
        filesViewModel.showSnackbarEvent.observe(this, Observer {
            if (it == true) {
                Snackbar.make(view!!, R.string.recording_deleted, Snackbar.LENGTH_SHORT).show()
                filesViewModel.doneShowingSnackbar()
            }
        })

        // Observer on the state variable for navigating when a list-item is clicked.
        filesViewModel.navigateToPlayerFragment.observe(this, Observer { recordingId ->
            recordingId?.let {
                this.findNavController().navigate(
                    FilesFragmentDirections
                        .actionFilesToPlayer(recordingId)
                )
                filesViewModel.onPlayerFragmentNavigated()
            }
        })

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        filesViewModel.initializeFrameLayout(files_layout)
        setupAdapter()
    }

    private fun setupAdapter() {
        adapter = RecordingItemAdapter(filesViewModel)
        binding.recordingList.adapter = adapter

        filesViewModel.allRecordings.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })
    }

    /**
     * Provides the Repository and context to the FilesViewModel.
     */
    class FilesViewModelFactory(
        private val dataSource: Repository,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FilesViewModel::class.java)) {
                return FilesViewModel(dataSource, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
