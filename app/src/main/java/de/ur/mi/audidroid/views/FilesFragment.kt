package de.ur.mi.audidroid.views

import android.app.Application
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.core.view.MenuItemCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.adapter.RecordingItemAdapter
import de.ur.mi.audidroid.databinding.FilesFragmentBinding
import de.ur.mi.audidroid.models.RecordingAndLabels
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.FilesDialog
import de.ur.mi.audidroid.utils.ConvertDialog
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
        setHasOptionsMenu(true)

        //Observer on the state variable for the sorting of list-items.
        filesViewModel.sortModus.observe(viewLifecycleOwner, Observer {
            filesViewModel.setSorting(it)
        })
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
            Observer { recordingId ->
                recordingId?.let {
                    this.findNavController().navigate(
                        FilesFragmentDirections
                            .actionFilesToPlayer(recordingId)
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

        return binding.root
    }

    // When the ImageButton is clicked, a PopupMenu opens.
    fun openPopupMenu(recordingAndLabels: RecordingAndLabels, view: View) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete_recording ->
                    filesViewModel.delete(recordingAndLabels)
                R.id.action_edit_recording ->
                    navigateToEditFragment(recordingAndLabels)
                R.id.action_share_recording -> {
                    filesViewModel.recordingToBeExported = recordingAndLabels
                    filesViewModel._createAlertDialog.value = true
                }
            }
            true
        }
        popupMenu.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_files, menu)

        val searchItem = menu.findItem(R.id.action_search)
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
            R.id.action_search ->{
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
        setupAdapter()
        createConfirmDialog()
    }

    private fun setupAdapter() {
        adapter = RecordingItemAdapter(this, filesViewModel)
        binding.recordingList.adapter = adapter

        filesViewModel.displayRecordings.observe(viewLifecycleOwner, Observer {
            it?.let {
                var array = arrayListOf<RecordingAndLabels>()
                array = filesViewModel.checkExistence(it, array)
                adapter.submitList(array)
            }
        })
    }

    private fun createConfirmDialog() {
        filesViewModel.createConfirmDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                FilesDialog.createDialog(
                    context = context!!,
                    type = R.string.confirm_dialog,
                    recording = filesViewModel.recording,
                    viewModel = filesViewModel,
                    errorMessage = filesViewModel.errorMessage
                )
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
