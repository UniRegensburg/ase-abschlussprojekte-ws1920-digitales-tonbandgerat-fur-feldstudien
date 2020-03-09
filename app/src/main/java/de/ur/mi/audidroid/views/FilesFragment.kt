package de.ur.mi.audidroid.views

import android.app.Activity
import android.app.Application
import android.content.Intent
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
import de.ur.mi.audidroid.adapter.Adapter
import de.ur.mi.audidroid.adapter.ExternalFolderAdapter
import de.ur.mi.audidroid.adapter.FolderAdapter
import de.ur.mi.audidroid.databinding.FilesFragmentBinding
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.ConvertDialog
import de.ur.mi.audidroid.utils.FolderDialog
import de.ur.mi.audidroid.utils.StorageHelper
import de.ur.mi.audidroid.viewmodels.FilesViewModel
import de.ur.mi.audidroid.viewmodels.FolderViewModel

/**
 * The fragment displays all recordings and folders.
 * @author: Theresa Strohmeier
 */
class FilesFragment : Fragment() {


    private lateinit var folderAdapter: FolderAdapter
    private lateinit var recordingAdapter: Adapter
    private lateinit var externalFolderAdapter: ExternalFolderAdapter
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

        val filesViewModelFactory = FilesViewModelFactory(dataSource, application)
        val folderViewModelFactory = FolderViewModelFactory(dataSource, application)
        filesViewModel = ViewModelProvider(this, filesViewModelFactory).get(FilesViewModel::class.java)
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

        folderViewModel.showSnackbarEvent.observe(viewLifecycleOwner, Observer {
            if(it == context!!.resources.getString(R.string.delete)){
                Snackbar.make(view!!,  R.string.folder_deleted, Snackbar.LENGTH_SHORT).show()
                folderViewModel.doneShowingSnackbar()
            }else if (it == context!!.resources.getString(R.string.create_folder)){
                Snackbar.make(view!!,  R.string.folder_created, Snackbar.LENGTH_SHORT).show()
                folderViewModel.doneShowingSnackbar()
            }
        })
        folderViewModel.initFolderSorting()

        folderViewModel.showSnackbarEvent.observe(viewLifecycleOwner, Observer {})
        folderViewModel.allFolders.observe(viewLifecycleOwner, Observer {})
        //folderViewModel.allInternalFolders.observe(viewLifecycleOwner, Observer {})
        //folderViewModel.allExternalFolders.observe(viewLifecycleOwner, Observer {container?.invalidate()})
        folderViewModel.allInternalFoldersSorted.observe(viewLifecycleOwner, Observer {  })
        folderViewModel.allExternalFoldersSorted.observe(viewLifecycleOwner, Observer {  })
        filesViewModel.allRecordings.observe(viewLifecycleOwner, Observer {container?.invalidate()})
        filesViewModel.allRecordingsWithNoFolder.observe(viewLifecycleOwner, Observer {})

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
        //Dialog for conversion.
        filesViewModel.createAlertConvertDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                ConvertDialog.createDialog(
                    context = context!!,
                    layoutId = R.layout.convert_dialog,
                    viewModel = filesViewModel
                )
            }
        })

        // alerts the dialog, that a recording should be moved
        filesViewModel.createAlertFolderDialog.observe(viewLifecycleOwner, Observer {
            if (it){
               FolderDialog.createDialog(
                   context = context!!,
                   type = R.string.alert_dialog,
                   folderViewModel =  folderViewModel,
                   filesViewModel = filesViewModel,
                   errorMessage = filesViewModel.errorMessage,
                   layoutId = R.layout.folder_dialog,
                   recordingToBeMoved = filesViewModel.recordingToBeMoved,
                   listOfAvailableFolders = folderViewModel.allFolders.value
               )
            }
        })

        folderViewModel.createAlertFolderDialog.observe(this, Observer {
            if (it){
                FolderDialog.createDialog(
                    context = context!!,
                    type = R.string.alert_dialog,
                    folderViewModel =  folderViewModel,
                    filesViewModel = filesViewModel,
                    errorMessage = folderViewModel.errorMessage,
                    addFolder = folderViewModel.addFolder,
                    layoutId = R.layout.folder_dialog,
                    folderToBeEdited = folderViewModel.folderToBeEdited)

            }
        })

        folderViewModel.createConfirmDialog.observe(this, Observer {
            if (it){
                FolderDialog.createDialog(
                    context = context!!,
                    type = R.string.confirm_dialog,
                    folderViewModel = folderViewModel,
                    filesViewModel = filesViewModel,
                    errorMessage = folderViewModel.errorMessage,
                    folderToBeEdited = folderViewModel.folderToBeEdited,
                    listOfAvailableFolders = folderViewModel.allFolders.value)
            }
        })

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupAdapter()
    }

    private fun setupAdapter() {
        val filesViewModel = binding.filesViewModel
        val folderViewModel = binding.folderViewModel

        if (filesViewModel != null && folderViewModel != null) {
            folderAdapter = FolderAdapter(filesViewModel, folderViewModel)
            recordingAdapter = Adapter(filesViewModel)
            externalFolderAdapter = ExternalFolderAdapter(filesViewModel, folderViewModel)

            //binding of adapters to Views
            binding.recordingListNoFolder.adapter = recordingAdapter
            binding.folderList.adapter = folderAdapter
            binding.externalFolderList.adapter = externalFolderAdapter
            binding.addExternalFolder.setOnClickListener { _ -> onClickAddExternalFolder() }

            //Sets Adapter to RecyclingView for Recordings with no folder association
            filesViewModel.allRecordingsWithNoFolder.observe(viewLifecycleOwner, Observer {
                it?.let {
                    recordingAdapter.submitList(it)

                }
            })

            //Sets Adapters to RecyclingView containing the known folders and their content
            folderViewModel.allInternalFoldersSorted.observe(viewLifecycleOwner, Observer {
                it?.let {
                    folderAdapter.submitList(it)
                    view!!.invalidate()

                }
            })

            folderViewModel.allExternalFoldersSorted.observe(viewLifecycleOwner, Observer {
                it?.let {
                    externalFolderAdapter.submitList(it)
                    view!!.invalidate()
                }
            })
        }
    }

    /**
     * Provides the access to an external folder.
     */
    private fun onClickAddExternalFolder(){
        startActivityForResult(StorageHelper.setOpenDocumentTreeIntent(),
            resources.getInteger(R.integer.activity_request_code_external_folder))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == resources.getInteger(R.integer.activity_request_code_external_folder) &&
            resultCode == Activity.RESULT_OK){
            folderViewModel.handleActivityResult(data!!.dataString!!)
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
