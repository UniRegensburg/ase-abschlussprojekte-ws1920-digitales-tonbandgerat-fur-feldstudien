package de.ur.mi.audidroid.views

import android.app.Application
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import de.ur.mi.audidroid.databinding.FilesFragmentBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import de.ur.mi.audidroid.adapter.EntryAdapter
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.adapter.RecordingListener
import de.ur.mi.audidroid.models.EntryRepository
import de.ur.mi.audidroid.viewmodels.FilesViewModel

class FilesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FilesFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.files_fragment, container, false)

        val application = requireNotNull(this.activity).application

        val dataSource = EntryRepository(application)
        val viewModelFactory = FilesViewModelFactory(dataSource, application)

        val filesViewModel = ViewModelProviders.of(this, viewModelFactory).get(FilesViewModel::class.java)

        binding.filesViewModel = filesViewModel

        val adapter = EntryAdapter(RecordingListener {  recordingPath ->
            //Toast.makeText(context, "${uId}", Toast.LENGTH_SHORT).show()
            filesViewModel.onRecordingClicked(recordingPath)
        })
        binding.recordingList.adapter = adapter

        filesViewModel.allRecordings.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        binding.setLifecycleOwner(this)

        filesViewModel.navigateToReplayFragment.observe(this, Observer { recordingPath ->
            recordingPath?.let {
                this.findNavController().navigate(
                    FilesFragmentDirections
                        .actionFilesToReplay(recordingPath))
                filesViewModel.onReplayFragmentNavigated()
            }
        })

        return binding.root
    }

    class FilesViewModelFactory(private val dataSource: EntryRepository, private val application: Application) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T: ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(FilesViewModel::class.java)) {
                return FilesViewModel(dataSource, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
