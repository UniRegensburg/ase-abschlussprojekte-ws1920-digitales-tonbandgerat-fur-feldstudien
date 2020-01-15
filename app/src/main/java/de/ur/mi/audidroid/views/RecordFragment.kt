package de.ur.mi.audidroid.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
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

    private var isRecording = false
    private lateinit var binding: RecordFragmentBinding

    companion object {
        fun newInstance() = RecordFragment()
    }

    private lateinit var viewModel: RecordViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.record_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RecordViewModel::class.java)
        initializeRecorderFunctionality()
    }

    private fun initializeRecorderFunctionality() {
        viewModel.initializeTimer(chronometer)
        record_pause_button.setOnClickListener {
            when (!isRecording) {
                true -> {
                    viewModel.recordButtonClicked(context!!)
                    binding.isVisible = true
                    isRecording = true
                }
                false -> {
                    viewModel.pauseButtonClicked()
                    isRecording = false
                }
            }
            binding.isRecording = isRecording
        }
        confirm_button.setOnClickListener {
            viewModel.confirmRecord(context!!)
            resetRecorder()
        }
        cancel_button.setOnClickListener {
            viewModel.cancelRecord(context!!)
            resetRecorder()
        }
    }

    private fun resetRecorder() {
        binding.isVisible = false
        isRecording = false
        binding.isRecording = isRecording
    }
}
