package de.ur.mi.audidroid.views

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.adapter.FolderAdapter
import de.ur.mi.audidroid.adapter.RecordingItemAdapter
import de.ur.mi.audidroid.databinding.FilesFragmentBinding
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.models.RecordingAndLabels
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.FilesDialog
import de.ur.mi.audidroid.utils.ConvertDialog
import de.ur.mi.audidroid.utils.FolderDialog
import de.ur.mi.audidroid.utils.StorageHelper
import de.ur.mi.audidroid.utils.FilterDialog
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
    private lateinit var binding: FilesFragmentBinding
    private lateinit var folderViewModel: FolderViewModel
    private lateinit var filesViewModel: FilesViewModel
    private lateinit var dataSource: Repository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val application = this.activity!!.application
        dataSource = Repository(application)
        binding = DataBindingUtil.inflate(inflater, R.layout.files_fragment, container, false)
        val filesViewModelFactory = FilesViewModelFactory(dataSource, application)
        val folderViewModelFactory = FolderViewModelFactory(dataSource, application)
        filesViewModel = ViewModelProvider(this, filesViewModelFactory).get(FilesViewModel::class.java)
        folderViewModel = ViewModelProvider(this, folderViewModelFactory).get(FolderViewModel::class.java)

        binding.folderViewModel = folderViewModel
        binding.filesViewModel = filesViewModel
        binding.filesFragment = this
        binding.lifecycleOwner = this
        setHasOptionsMenu(true)

        observeSnackBars()
        observeMinutiaeData()
        observeNavigation()

        return binding.root
    }

    private fun observeNavigation(){
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
    }

    private fun observeMinutiaeData(){
        filesViewModel.allRecordingsWithMarker.observe(viewLifecycleOwner, Observer {})
        //Observer on the circumstances regarding the move between external folders.
        folderViewModel.externalFolderLiveData.observe(viewLifecycleOwner, Observer {
            folderViewModel.externalFolderCount = it
        })
        //Observer on the state variable for the sorting of list-items.
        filesViewModel.sortModus.observe(viewLifecycleOwner, Observer {
            filesViewModel.setSorting(it)
        })
    }

    private fun observeSnackBars(){
        filesViewModel.showSnackbarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Snackbar.make(view!!, R.string.recording_deleted, Snackbar.LENGTH_SHORT).show()
                filesViewModel.doneShowingSnackbar()
            }
        })
        folderViewModel.showSnackbarEvent.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()){
                Snackbar.make(view!!, it, Snackbar.LENGTH_SHORT).show()
                folderViewModel.doneShowingSnackbar()
            }
        })
    }


    // When the ImageButton is clicked, a PopupMenu opens.
    fun openRecordingPopupMenu(recordingAndLabels: RecordingAndLabels, view: View) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        setRecordingMenuOptions(popupMenu, recordingAndLabels.recordingPath)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete_recording ->{
                    filesViewModel.delete(recordingAndLabels)
                    recordingAndLabels.folder?.let { folderViewModel.updateFolderCount(recordingAndLabels.folder,null) }
                }
                R.id.action_edit_recording ->
                    navigateToEditFragment(recordingAndLabels)
                R.id.action_share_recording -> {
                    filesViewModel.recordingToBeExported = recordingAndLabels
                    filesViewModel.createAlertConvertDialog.value = true}
                R.id.action_move_recording -> {
                        filesViewModel.recordingToBeMoved = recordingAndLabels
                        recordingAndLabels.folder?.let { folderViewModel.getOldFolder(recordingAndLabels.folder) }
                        filesViewModel.createAlertFolderDialog.value = true
                }
            }
            true
        }
        popupMenu.show()
    }

    //Modifies the folder options of a recording depending on the situation.
    private fun setRecordingMenuOptions(popupMenu: PopupMenu,  path: String){
        val allFolders = folderViewModel.allFolders.value!!
        if (allFolders.isEmpty()){
            popupMenu.menu.findItem(R.id.action_move_recording).isVisible = false
        }
        if (path.startsWith(context!!.resources.getString(R.string.content_uri_prefix))){
            val externalFolderCount = folderViewModel.externalFolderCount
            if (externalFolderCount <= 1){
                popupMenu.menu.findItem(R.id.action_move_recording).isVisible = false}
        }
    }

    fun openFolderPopupMenu(folder: FolderEntity, view: View){
        val popupMenu = PopupMenu(context, view)
        folderViewModel.folderToBeEdited = folder
        popupMenu.menuInflater.inflate(R.menu.popup_menu_folder, popupMenu.menu)
        if(folder.parentDir != null || folder.isExternal){
            popupMenu.menu.findItem(R.id.action_add_subfolder).isVisible = false
        }
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

    fun addFolderPopupMenu(view: View) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu_add_folder, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_add_folder_int ->
                    folderViewModel.onAddInternalFolderClicked()
                R.id.action_add_folder_ext ->
                    onClickAddExternalFolder()
            }
            true
        }
        popupMenu.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_files, menu)
        
        val searchItem: MenuItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.isNotEmpty()){
                    filesViewModel.setSearchResult(newText)
                }else{ filesViewModel._sortModus.value = null }
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter ->{
                filesViewModel._createFilterDialog.value = true
                true
            }
            R.id.action_sort_name -> {
                filesViewModel._sortModus.value = context!!.resources.getInteger(R.integer.sort_by_name)
                true
            }
            R.id.action_sort_date -> {
                filesViewModel._sortModus.value = context!!.resources.getInteger(R.integer.sort_by_date)
                true
            }
            R.id.action_sort_duration -> {
                filesViewModel._sortModus.value = context!!.resources.getInteger(R.integer.sort_by_duration)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    private fun navigateToEditFragment(recordingAndLabels: RecordingAndLabels) {
        this.findNavController().navigate(
            FilesFragmentDirections.actionFilesToEdit(recordingAndLabels.uid)
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        filesViewModel.initializeFrameLayout(files_layout)
        filesViewModel.setSorting(null)
        folderViewModel.sortAllFolders()
        setupAdapter()
        createConfirmDialog()
        FilterDialog.clearDialog()
    }

    private fun setupAdapter() {
        val filesViewModel = binding.filesViewModel
        val folderViewModel = binding.folderViewModel
        if (filesViewModel != null && folderViewModel != null) {
            recordingAdapter = RecordingItemAdapter(this, filesViewModel)
            folderAdapter = FolderAdapter(this, filesViewModel, folderViewModel)

            binding.recordingListDisplay.adapter = recordingAdapter
            binding.folderList.adapter = folderAdapter

            filesViewModel.displayRecordings.observe(viewLifecycleOwner, Observer {
                val recordings = arrayListOf<RecordingAndLabels>()
                it?.let {
                    it.forEach { recording -> if (recording.folder == null){ recordings.add(recording)} }
                }
                recordingAdapter.submitList(recordings)
            })
            folderViewModel.allFoldersSorted.observe(viewLifecycleOwner, Observer {values ->
                val folders = arrayListOf<FolderEntity>()
                values?.let { list -> list.forEach { if (it.isShown){folders.add(it)} } }
                folderAdapter.submitList(folders)
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
                    folderToBeEdited = folderViewModel.folderToBeEdited,
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
                    folderToBeEdited = folderViewModel.folderToBeEdited
                )
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
                    listOfAvailableFolders = folderViewModel.allFolders.value
                )
            }
        })
        //Dialog for filtering recordings.
        filesViewModel.createFilterDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                FilterDialog.createDialog(
                    context = context!!,
                    layoutId = R.layout.filter_dialog,
                    viewModel = filesViewModel,
                    dataSource = dataSource,
                    fragment = this
                )
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
