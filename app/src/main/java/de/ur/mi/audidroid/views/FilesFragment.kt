package de.ur.mi.audidroid.views

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.adapter.ExternalFolderAdapter
import de.ur.mi.audidroid.adapter.FolderAdapter
import de.ur.mi.audidroid.adapter.RecordingItemAdapter
import de.ur.mi.audidroid.databinding.FilesFragmentBinding
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.FilesDialog
import de.ur.mi.audidroid.utils.ConvertDialog
import de.ur.mi.audidroid.utils.FolderDialog
import de.ur.mi.audidroid.utils.StorageHelper
import de.ur.mi.audidroid.viewmodels.FilesViewModel
import de.ur.mi.audidroid.viewmodels.FolderViewModel
import kotlinx.android.synthetic.main.files_fragment.*

/**
 * The fragment displays all recordings and folders.
 * @author: Theresa Strohmeier
 */
class FilesFragment : Fragment() {


    private lateinit var folderAdapter: FolderAdapter
    private lateinit var recordingAdapter: RecordingItemAdapter
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

        filesViewModel.initDisplay()
        folderViewModel.initFolderSorting()

        folderViewModel.showSnackbarEvent.observe(viewLifecycleOwner, Observer {})
        folderViewModel.allFolders.observe(viewLifecycleOwner, Observer {})
        folderViewModel.allInternalFoldersSorted.observe(viewLifecycleOwner, Observer {  })
        folderViewModel.allExternalFoldersSorted.observe(viewLifecycleOwner, Observer {  })
        filesViewModel.allRecordings.observe(viewLifecycleOwner, Observer {container?.invalidate()})
        filesViewModel.allRecordingsWithNoFolder.observe(viewLifecycleOwner, Observer {})

        observeSnackBars()

        // Observer on the state variable for navigating when a list-item is clicked.
        filesViewModel.navigateToPlayerFragment.observe(
            viewLifecycleOwner,
            Observer { recordingId ->
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

    private fun observeSnackBars(){
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
    }

    // When the ImageButton is clicked, a PopupMenu opens.
    fun openRecordingPopupMenu(entryEntity: EntryEntity, view: View) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete_recording ->
                    filesViewModel.delete(entryEntity)
                R.id.action_edit_recording ->
                    navigateToEditFragment(entryEntity)
                R.id.action_move_recording -> {
                    filesViewModel.recordingToBeMoved = entryEntity
                    filesViewModel.createAlertFolderDialog.value = true
                }
                R.id.action_share_recording -> {
                    filesViewModel.recordingToBeExported = entryEntity
                    filesViewModel.createAlertConvertDialog.value = true
                }
            }
            true
        }
        popupMenu.show()
    }

    fun openFolderPopupMenu(folder: FolderEntity, view: View){
        val popupMenu = PopupMenu(context, view)
        folderViewModel.folderToBeEdited = folder
        popupMenu.menuInflater.inflate(R.menu.popup_menu_folder, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId){
                R.id.action_add_subfolder ->
                    folderViewModel.onAddFolderClicked(folder)
                R.id.action_delete_folder ->
                    folderViewModel.onDeleteFolderClicked(folder, view)
            }
            true
        }
        popupMenu.show()
    }

    /*
    fun openSortByPopupMenu(view: View){
        val popupMenu = PopupMenu(context, view)

        popupMenu.menuInflater.inflate(R.menu.popup_menu_sort_by, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId){
                R.id.action_sort_by_date ->
                    println("BY DATE")
                R.id.action_sort_by_name ->
                    println("BY NAME")
                R.id.action_sort_by_duration ->
                    println("BY DURATION")
            }
            true
        }
        popupMenu.show()
    }*/

    private fun navigateToEditFragment(entryEntity: EntryEntity) {
        this.findNavController().navigate(
            FilesFragmentDirections.actionFilesToEdit(entryEntity.uid)
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        filesViewModel.initializeFrameLayout(files_layout)
        setupAdapter()
        createConfirmDialog()
    }

    private fun setupAdapter() {
        val filesViewModel = binding.filesViewModel
        val folderViewModel = binding.folderViewModel

        if (filesViewModel != null && folderViewModel != null) {
            recordingAdapter = RecordingItemAdapter(this, filesViewModel)
            folderAdapter = FolderAdapter(this, filesViewModel, folderViewModel)
            externalFolderAdapter = ExternalFolderAdapter(this, filesViewModel, folderViewModel)

            binding.recordingListDisplay.adapter = recordingAdapter
            binding.folderList.adapter = folderAdapter
            binding.externalFolderList.adapter = externalFolderAdapter
            binding.addExternalFolder.setOnClickListener { _ -> onClickAddExternalFolder() }

            //Sets Adapter to RecyclingView for Recordings with no folder association.
           filesViewModel.displayRecordings.observe(viewLifecycleOwner, Observer {
               it?.let {
                   recordingAdapter.submitList(it)
                   view!!.invalidate()
               }
           })
            /*
            filesViewModel.allRecordingsWithNoFolder.observe(viewLifecycleOwner, Observer {
                it?.let {
                    var array = arrayListOf<EntryEntity>()
                    array = filesViewModel.checkExistence(it, array)
                    recordingAdapter.submitList(array)

                }
            })*/

            //Sets Adapters to RecyclingView containing the known folders and their content.
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

    private fun createConfirmDialog() {

        //Dialog for deletion of recording.
        filesViewModel.createConfirmDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                FilesDialog.createFilesDialog(
                    context = context!!,
                    type = R.string.confirm_dialog,
                    recording = filesViewModel.recording,
                    viewModel = filesViewModel,
                    errorMessage = filesViewModel.errorMessage
                )
            }
        })
        //Dialog for conversion of recording.
        filesViewModel.createAlertConvertDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                ConvertDialog.createDialog(
                    context = context!!,
                    layoutId = R.layout.convert_dialog,
                    viewModel = filesViewModel
                )
            }
        })
        //Dialog for movement of recording.
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
        //Dialog for creation of folder.
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
        //Dialog for deletion of folder.
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
    }

   //Allows the creation of a new external folder.
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
