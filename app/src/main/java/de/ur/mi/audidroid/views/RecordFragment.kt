package de.ur.mi.audidroid.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.viewmodels.RecordViewModel
import kotlinx.android.synthetic.main.record_fragment.*
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import de.ur.mi.audidroid.databinding.RecordFragmentBinding


/**
 * The fragment allows the user to do a voice recording. The changes of the view are handled within.
 * @author: Sabine Roth
 */

class RecordFragment : Fragment() {

    private var isRecording = false

    companion object {
        fun newInstance() = RecordFragment()
    }

    private lateinit var viewModel: RecordViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: RecordFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.record_fragment, container, false)
        binding.isVisible = false
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RecordViewModel::class.java)
        initializeRecorder()
    }

    private fun initializeRecorder() {
        viewModel.initializeRecorder(context!!)
        record_pause_button.setOnClickListener {
            viewModel.recordPauseButtonClicked(record_pause_button)
            if (!isRecording) {
                //binding show Buttons
            }
            isRecording = true
        }
        confirm_button.setOnClickListener {
            viewModel.confirmRecord(context!!)
            viewModel.initializeRecorder(context!!)
            record_pause_button.setImageResource(R.mipmap.recording_button_foreground)
            //binding hide buttons
            isRecording = false
        }
        cancel_button.setOnClickListener {
            viewModel.cancelRecord(context!!)
            viewModel.initializeRecorder(context!!)
            record_pause_button.setImageResource(R.mipmap.recording_button_foreground)
            //binding hide buttons
            isRecording = false
        }
    }


    /** Depending on the current state of the recording various buttons are visible or not */
    // @BindingAdapter("android:visibility")
    /*  private fun toggleVisibility(view: View, visible:Boolean) {

          view.visibility = if(visible) View.VISIBLE else View.GONE


          confirm_button.visibility =
              if (confirm_button.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
          cancel_button.visibility =
              if (cancel_button.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
      }*/
}
