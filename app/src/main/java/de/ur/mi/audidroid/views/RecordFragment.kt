package de.ur.mi.audidroid.views

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import androidx.core.view.size
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.adapter.MarkerButtonAdapter
import de.ur.mi.audidroid.adapter.MarkerItemAdapter
import de.ur.mi.audidroid.databinding.MarkerButtonBinding
import de.ur.mi.audidroid.databinding.RecordFragmentBinding
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.viewmodels.RecordViewModel
import kotlinx.android.synthetic.main.record_fragment.*

/**
 * The fragment allows the user to do a voice recording. The changes of the view are handled within.
 * The view changes automatically with data binding depending on the current state
 * @author: Sabine Roth
 */

class RecordFragment : Fragment() {

    private lateinit var viewModel: RecordViewModel
    private lateinit var adapter: MarkerButtonAdapter
    private lateinit var binding: RecordFragmentBinding

    companion object {
        fun newInstance() = RecordFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val application = this.activity!!.application
        val dataSource = Repository(application)
        binding = DataBindingUtil.inflate(inflater, R.layout.record_fragment, container, false)

        val viewModelFactory = RecordViewModelFactory(dataSource, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(RecordViewModel::class.java)
        binding.recordViewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    class RecordViewModelFactory(
        private val dataSource: Repository,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(RecordViewModel::class.java)) {
                RecordViewModel(dataSource, application) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.initializeTimer(chronometer)
        viewModel.initializeLayout(frameLayout)
        setupAdapter()
        viewModel.createDialog.observe(viewLifecycleOwner, Observer {
            if (it) {
                de.ur.mi.audidroid.utils.Dialog.createDialog(
                    context = context!!,
                    layoutId = R.layout.dialog_save_recording,
                    viewModel = viewModel,
                    errorMessage = viewModel.errorMessage
                )
            }
        })
    }

    private fun setupAdapter() {
        adapter = MarkerButtonAdapter(viewModel)
        binding.markerButtonList.adapter = adapter

        val layoutManager = GridLayoutManager(context, 3)
        marker_button_list.layoutManager = layoutManager

        viewModel.allMarkers.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
                layoutManager.spanCount = getSpanCount(it.size)
            }
        })
    }

    private fun getSpanCount(numberOfItems: Int): Int {
        return when (numberOfItems) {
            1 -> 1
            2 -> 2
            else -> 3
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.cancelRecord()
    }
}
