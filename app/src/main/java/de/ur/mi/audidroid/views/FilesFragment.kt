package de.ur.mi.audidroid.views

import android.app.Application
import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Color
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
import de.ur.mi.audidroid.adapter.RecordingAndFolderActionsListener
import de.ur.mi.audidroid.adapter.RecordingAndFolderAdapter
import de.ur.mi.audidroid.databinding.FilesFragmentBinding
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.models.RecordingAndLabels
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.FilesDialog
import de.ur.mi.audidroid.utils.ConvertDialog
import de.ur.mi.audidroid.utils.FolderDialog
import de.ur.mi.audidroid.utils.FilterDialog
import de.ur.mi.audidroid.utils.RenameDialog
import de.ur.mi.audidroid.viewmodels.FilesViewModel
import kotlinx.android.synthetic.main.files_fragment.*

/**
 * The fragment displays all recordings.
 * @author: Theresa Strohmeier
 */
class FilesFragment : Fragment() {

    private lateinit var adapter: RecordingAndFolderAdapter
    private lateinit var binding: FilesFragmentBinding
    private lateinit var filesViewModel: FilesViewModel
    private lateinit var dataSource: Repository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.files_fragment, container, false)

        val application = requireActivity().application
        dataSource = Repository(application)

        val viewModelFactory = FilesViewModelFactory(dataSource, application)
        filesViewModel = ViewModelProvider(this, viewModelFactory).get(FilesViewModel::class.java)

        binding.filesViewModel = filesViewModel
        binding.lifecycleOwner = this
        setHasOptionsMenu(true)

        filesViewModel.allRecordingsWithMarker.observe(viewLifecycleOwner, Observer {})
        //Observer on the state variable for the sorting of list-items.
        filesViewModel.sortMode.observe(viewLifecycleOwner, Observer {
            filesViewModel.setSorting(it)
        })
        //Observer on the state variable for showing Snackbar message when a list-item is deleted.
        filesViewModel.showSnackbarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Snackbar.make(requireView(), R.string.recording_deleted, Snackbar.LENGTH_SHORT)
                    .show()
                filesViewModel.doneShowingSnackbar()
            }
        })

        // Observer on the state variable for navigating when a list-item is clicked.
        filesViewModel.navigateToPlayerFragment.observe(
            viewLifecycleOwner,
            Observer { recordingId ->
                recordingId?.let {
                    this.findNavController().navigate(
                        FilesFragmentDirections
                            .actionFilesToPlayer(
                                recordingId[0].toInt(),
                                recordingId[1],
                                recordingId[2]
                            )
                    )
                    filesViewModel.onPlayerFragmentNavigated()
                }
            })

        filesViewModel.createAlertDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                ConvertDialog.createDialog(
                    context = requireContext(),
                    layoutId = R.layout.convert_dialog,
                    viewModel = filesViewModel
                )
            }
        })

        filesViewModel._currentlyInFolder.value = false

        return binding.root
    }

    // When the ImageButton is clicked, a PopupMenu opens.
    fun openRecordingPopupMenu(recordingAndLabels: RecordingAndLabels, view: View) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_rename_recording ->
                    filesViewModel.rename(recordingAndLabels)
                R.id.action_delete_recording ->
                    filesViewModel.delete(recordingAndLabels)
                R.id.action_edit_recording ->
                    navigateToEditFragment(recordingAndLabels)
                R.id.action_share_recording -> {
                    filesViewModel.recordingToBeExported = recordingAndLabels
                    filesViewModel._createAlertDialog.value = true
                }
//                R.id.action_move_recording -> filesViewModel.moveRecording(recordingAndLabels.uid)
            }
            true
        }
        popupMenu.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_files, menu)

        val searchItem: MenuItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.isNotEmpty()) {
                    filesViewModel.setSearchResult(newText)
                } else {
                    filesViewModel._sortMode.value = null
                }
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                filesViewModel._createFilterDialog.value = true
                true
            }
            R.id.action_sort_name -> {
                filesViewModel._sortMode.value =
                    requireContext().resources.getInteger(R.integer.sort_by_name)
                true
            }
            R.id.action_sort_date -> {
                filesViewModel._sortMode.value =
                    requireContext().resources.getInteger(R.integer.sort_by_date)
                true
            }
            R.id.action_sort_duration -> {
                filesViewModel._sortMode.value =
                    requireContext().resources.getInteger(R.integer.sort_by_duration)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToEditFragment(recordingAndLabels: RecordingAndLabels) {
        this.findNavController().navigate(
            FilesFragmentDirections.actionFilesToEdit(
                recordingAndLabels.uid,
                recordingAndLabels.recordingName,
                recordingAndLabels.recordingPath
            )
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        filesViewModel.initializeFrameLayout(files_layout)
        filesViewModel.setSorting(null)
        setupAdapter()
        adaptObservers()
        FilterDialog.clearDialog()
    }

    private fun setupAdapter() {
        adapter = RecordingAndFolderAdapter(requireContext(), filesViewModel, userActionsListener)
        binding.recordingList.adapter = adapter

        filesViewModel.displayRecordingsAndFolders.observe(viewLifecycleOwner, Observer {
            it?.let {
                val filterEntries = filesViewModel.getCorrectList(it)
                adapter.submitList(filterEntries)
            }
        })
    }

    private fun adaptObservers() {
        filesViewModel.createConfirmDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                FilesDialog.createDialog(
                    context = requireContext(),
                    type = R.string.confirm_dialog,
                    recording = filesViewModel.recording,
                    viewModel = filesViewModel,
                    errorMessage = filesViewModel.errorMessage
                )
            }
        })

        filesViewModel.createFilterDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                FilterDialog.createDialog(
                    context = requireContext(),
                    layoutId = R.layout.filter_dialog,
                    viewModel = filesViewModel,
                    dataSource = dataSource,
                    fragment = this
                )
            }
        })

        filesViewModel.folderDialog.observe(viewLifecycleOwner, Observer {
            if(it){
                FolderDialog.createDialog(
                    context = requireContext(),
                    viewModel = filesViewModel,
                    errorMessage = filesViewModel.errorMessage,
                    layoutId = R.layout.folder_dialog_create,
                    folderToBeEdited = filesViewModel.folderToBeEdited,
                    deleteFolder = filesViewModel.deleteFolder
                )
            }
        })

        filesViewModel.createRenameDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                RenameDialog.createDialog(
                    context = requireContext(),
                    viewModel = filesViewModel,
                    recording = filesViewModel.recording,
                    errorMessage = filesViewModel.errorMessage
                )
            }
        })

        filesViewModel.currentlyInFolder.observe(viewLifecycleOwner, Observer {
            if (it) {
                folder_back_target.setOnDragListener { v, event ->
                    when (event.action) {
                        DragEvent.ACTION_DRAG_STARTED -> {
                            if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                                v.setBackgroundColor(Color.TRANSPARENT)
                                v.invalidate()
                                true
                            } else {
                                false
                            }
                        }
                        DragEvent.ACTION_DRAG_ENTERED -> {
                            v.setBackgroundColor(requireContext().getColor(R.color.color_primary))
                            v.invalidate()
                            true
                        }
                        DragEvent.ACTION_DRAG_LOCATION ->
                            true
                        DragEvent.ACTION_DRAG_EXITED -> {
                            v.setBackgroundColor(Color.TRANSPARENT)
                            v.invalidate()
                            true
                        }
                        DragEvent.ACTION_DROP -> {
                            val recordingId: ClipData.Item = event.clipData.getItemAt(1)
                            filesViewModel.removeRecordingFromFolder(filesViewModel.recordingToBeMoved!!)
                            v.invalidate()
                            true
                        }
                        DragEvent.ACTION_DRAG_ENDED -> {
                            v.setBackgroundColor(Color.TRANSPARENT)
                            v.invalidate()
                            true
                        }
                        else -> false
                    }
                }
            }
        })
    }

    private val userActionsListener = object : RecordingAndFolderActionsListener {
        override fun onRecordingClicked(recordingAndLabels: RecordingAndLabels) {
            filesViewModel.onRecordingClicked(recordingAndLabels.uid, recordingAndLabels.recordingName, recordingAndLabels.recordingPath)
        }

        override fun popUpRecording(recordingAndLabels: RecordingAndLabels, view: View) {
            openRecordingPopupMenu(recordingAndLabels, view)
        }

        override fun onFolderClicked(folder: FolderEntity) {
            filesViewModel.onFolderClicked(folder)
        }

        override fun popUpFolder(folder: FolderEntity, view: View) {
            filesViewModel.openFolderMenu(folder, view)
        }
    }

    override fun onPause() {
        filesViewModel._currentlyInFolder.value = false
        super.onPause()
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
