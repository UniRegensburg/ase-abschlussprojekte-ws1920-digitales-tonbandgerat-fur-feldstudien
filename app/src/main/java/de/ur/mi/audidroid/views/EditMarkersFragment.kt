package de.ur.mi.audidroid.views

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.adapter.MarkerItemAdapter
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.MarkersDialog
import de.ur.mi.audidroid.databinding.EditMarkersFragmentBinding
import de.ur.mi.audidroid.viewmodels.EditMarkersViewModel
import kotlinx.android.synthetic.main.edit_markers_fragment.*
import java.lang.IllegalArgumentException

class EditMarkersFragment : Fragment() {

    private lateinit var viewModel: EditMarkersViewModel
    private lateinit var adapter: MarkerItemAdapter
    private lateinit var binding: EditMarkersFragmentBinding

    companion object {
        fun newInstance() = EditMarkersFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val application = this.activity!!.application
        val dataSource = Repository(application)
        binding = DataBindingUtil.inflate(inflater, R.layout.edit_markers_fragment, container, false)

        val viewModelFactory =
            EditMarkersViewModelFactory(dataSource, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(EditMarkersViewModel::class.java)
        binding.editMarkersViewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.showSnackbarEvent.observe(viewLifecycleOwner, Observer {
            if (it) {
                Snackbar.make(view!!, R.string.label_deleted, Snackbar.LENGTH_SHORT).show()
                viewModel.doneShowingSnackbar()
            }
        })

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupAdapter()
        viewModel.initializeLayout(edit_markers_frame_layout)
        viewModel.createAlertDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                MarkersDialog.createDialog(
                    context = context!!,
                    type = R.string.alert_dialog,
                    markerToBeEdited = viewModel.markerToBeEdited,
                    layoutId = R.layout.markers_dialog,
                    viewModel = viewModel,
                    errorMessage = viewModel.errorMessage
                )
            }
        })
        viewModel.createConfirmDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                MarkersDialog.createDialog(
                    context = context!!,
                    type = R.string.confirm_dialog,
                    markerToBeEdited = viewModel.markerToBeEdited,
                    layoutId = null,
                    viewModel = viewModel,
                    errorMessage = viewModel.errorMessage
                )
            }
        })
    }

    private fun setupAdapter() {
        adapter = MarkerItemAdapter(viewModel)
        binding.markersList.adapter = adapter

        viewModel.allMarkers.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })
    }

    class EditMarkersViewModelFactory(
        private val dataSource: Repository,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(EditMarkersViewModel::class.java)) {
                EditMarkersViewModel(dataSource, application) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }
    }

}
