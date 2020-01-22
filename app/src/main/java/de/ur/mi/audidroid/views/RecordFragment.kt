package de.ur.mi.audidroid.views

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.databinding.RecordFragmentBinding
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: RecordFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.record_fragment, container, false)

        val viewModelFactory = RecordViewModelFactory(context!!, binding)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(RecordViewModel::class.java)
        binding.recordViewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    class RecordViewModelFactory(
        private val context: Context,
        private val binding: RecordFragmentBinding
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(RecordViewModel::class.java)) {
                RecordViewModel(context, binding) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.initializeTimer(chronometer)
        viewModel.initializeLayout(frameLayout)
    }

    override fun onPause() {
        super.onPause()
        viewModel.cancelRecord()
    }
}
