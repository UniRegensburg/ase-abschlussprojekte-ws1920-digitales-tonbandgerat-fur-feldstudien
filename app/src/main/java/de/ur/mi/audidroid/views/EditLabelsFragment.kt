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
import de.ur.mi.audidroid.adapter.LabelItemAdapter
import de.ur.mi.audidroid.databinding.EditLabelsFragmentBinding
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.LabelsDialog
import de.ur.mi.audidroid.viewmodels.EditLabelsViewModel
import kotlinx.android.synthetic.main.edit_labels_fragment.*
import java.lang.IllegalArgumentException

class EditLabelsFragment : Fragment() {

    private lateinit var viewModel: EditLabelsViewModel
    private lateinit var adapter: LabelItemAdapter
    private lateinit var binding: EditLabelsFragmentBinding

    companion object {
        fun newInstance() = EditLabelsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val application = this.activity!!.application
        val dataSource = Repository(application)
        binding = DataBindingUtil.inflate(inflater, R.layout.edit_labels_fragment, container, false)

        val viewModelFactory = EditLabelsViewModelFactory(dataSource, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(EditLabelsViewModel::class.java)
        binding.editLabelsViewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.showSnackbarEvent.observe(this, Observer {
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
        viewModel.initializeLayout(edit_labels_frame_layout)
        viewModel.createAlertDialog.observe(this, Observer {
            if (it) {
                LabelsDialog.createDialog(
                    context = context!!,
                    type = R.string.alert_dialog,
                    labelToBeEdited = viewModel.labelToBeEdited,
                    layoutId = R.layout.labels_dialog,
                    viewModel = viewModel,
                    errorMessage = viewModel.errorMessage
                )
            }
        })
        viewModel.createConfirmDialog.observe(this, Observer {
            if (it) {
                LabelsDialog.createDialog(
                    context = context!!,
                    type = R.string.confirm_dialog,
                    labelToBeEdited = viewModel.labelToBeEdited,
                    layoutId = null,
                    viewModel = viewModel,
                    errorMessage = viewModel.errorMessage
                )
            }
        })
    }

    private fun setupAdapter() {
        adapter = LabelItemAdapter(viewModel)
        binding.labelsList.adapter = adapter

        viewModel.allLabels.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })
    }

    class EditLabelsViewModelFactory(
        private val dataSource: Repository,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(EditLabelsViewModel::class.java)) {
                EditLabelsViewModel(dataSource, application) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }
    }

}
