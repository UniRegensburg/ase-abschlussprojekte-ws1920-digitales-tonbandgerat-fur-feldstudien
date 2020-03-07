package de.ur.mi.audidroid.views

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.adapter.Adapter
import de.ur.mi.audidroid.adapter.FolderAdapter
import de.ur.mi.audidroid.databinding.FilesFragmentBinding
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.ConvertDialog
import de.ur.mi.audidroid.utils.FolderDialog
import de.ur.mi.audidroid.viewmodels.FilesViewModel
import de.ur.mi.audidroid.viewmodels.FolderViewModel

/**
 * The fragment displays all recordings.
 * @author: Theresa Strohmeier
 */
class FilesFragment : Fragment() {

    private lateinit var adapter: Adapter

    private lateinit var folderAdapter: FolderAdapter
    private lateinit var recordingAdapter: Adapter
    private lateinit var binding: FilesFragmentBinding
    private lateinit var folderViewModel: FolderViewModel
    private lateinit var filesViewModel: FilesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val application = this.activity!!.application
        val dataSource = Repository(application)
        binding = DataBindingUtil.inflate(inflater, R.layout.files_fragment, container, false)


        val viewModelFactory = FilesViewModelFactory(dataSource, application)
        val folderViewModelFactory = FolderViewModelFactory(dataSource, application)
        filesViewModel = ViewModelProvider(this, viewModelFactory).get(FilesViewModel::class.java)
        folderViewModel = ViewModelProvider(this, folderViewModelFactory).get(FolderViewModel::class.java)

        binding.folderViewModel = folderViewModel
        binding.filesViewModel = filesViewModel

        binding.lifecycleOwner = this

        //Observer on the state variable for showing Snackbar message when a list-item is deleted.
        filesViewModel.showSnackbarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Snackbar.make(view!!, R.string.recording_deleted, Snackbar.LENGTH_SHORT).show()
                filesViewModel.doneShowingSnackbar()
            }
        })

        // Observer on the state variable for navigating when a list-item is clicked.
        filesViewModel.navigateToPlayerFragment.observe(
            viewLifecycleOwner,
            Observer { recordingPath ->
                recordingPath?.let {
                    this.findNavController().navigate(
                        FilesFragmentDirections
                            .actionFilesToPlayer(recordingPath)
                    )
                    filesViewModel.onPlayerFragmentNavigated()
                }
            })

        filesViewModel.createAlertDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                ConvertDialog.createDialog(
                    context = context!!,
                    layoutId = R.layout.convert_dialog,
                    viewModel = filesViewModel
                )
            }
        })
        //calls dialog for creating an in internal folder
        folderViewModel.createAlertDialog.observe(this, Observer {
            if (it){
                FolderDialog.createDialog(
                    context = context!!,
                    type = R.string.alert_dialog,
                    folderToBeEdited = folderViewModel.folderToBeEdited,
                    layoutId = R.layout.folder_dialog,
                    viewModel = folderViewModel,
                    errorMessage = folderViewModel.errorMessage,
                    folderToBeCreated = folderViewModel.folderToBeCreated)

            }
        })
        //calls dialog for deleting a new folder
        folderViewModel.createConfirmDialog.observe(this, Observer {
            if (it){
                FolderDialog.createDialog(
                    context = context!!,
                    type = R.string.confirm_dialog,
                    folderToBeEdited = folderViewModel.folderToBeEdited,
                    layoutId = null,
                    viewModel = folderViewModel,
                    filesViewModel = filesViewModel,
                    errorMessage = folderViewModel.errorMessage)
            }
        })
        //calls dialog for moving a recording
        filesViewModel.folderToBeMoved.observe(this, Observer {
            if (it != null){
                FolderDialog.createDialog(
                    context = context!!,
                    type = R.string.alert_dialog,
                    viewModel = folderViewModel,
                    errorMessage = folderViewModel.errorMessage,
                    entryToBeMoved = it,
                    listOfAvailableFolders = folderViewModel.allFolders.value
                    //listOfAvailableFolders = filesViewModel.allFolders.value
                )
            }
        })

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        folderViewModel.initFolderSorting()
        setupAdapter()
    }

    private fun setupAdapter() {
        val filesViewModel = binding.filesViewModel
        val folderViewModel = binding.folderViewModel

        if (filesViewModel != null && folderViewModel != null) {
            folderAdapter = FolderAdapter(filesViewModel, folderViewModel)
            recordingAdapter = Adapter(filesViewModel)

            //binding of adapters to Views
            binding.recordingListNoFolder.adapter = recordingAdapter
            binding.folderList.adapter = folderAdapter

            //Sets Adapter to RecyclingView for Recordings with no folder association
            filesViewModel.allRecordingsWithNoFolder.observe(viewLifecycleOwner, Observer {
                it?.let {
                    recordingAdapter.submitList(it)

                }
            })

            //Sets Adapter to RecyclingView containing the known folders and their content
           // val folders = folderViewModel.initFolderSorting()
            val folders = folderViewModel.initFolderSorting()
            folders.observe(viewLifecycleOwner, Observer {
                it?.let {
                    folderAdapter.submitList(it)
                }
            })
        }
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

    class FolderViewModelFactory(
        private val dataSource: Repository,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FolderViewModel::class.java)) {
                return FolderViewModel(dataSource, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
