package de.ur.mi.audidroid.views

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.ur.mi.audidroid.R
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
        val binding: RecordFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.record_fragment, container, false)

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
        viewModel.createDialog.observe(this, Observer {
            if (it) {
                de.ur.mi.audidroid.utils.Dialog.createDialog(
                    paramContext = context!!,
                    layoutId = R.layout.dialog_save_recording,
                    viewModel = viewModel,
                    errorMessage = viewModel.errorMessage,
                    recordFragment = this
                )
            }
        })
    }

    override fun onPause() {
        super.onPause()
        viewModel.cancelRecord()
    }
}
