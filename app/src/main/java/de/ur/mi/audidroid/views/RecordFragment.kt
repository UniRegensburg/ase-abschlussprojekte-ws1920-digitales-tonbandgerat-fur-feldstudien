package de.ur.mi.audidroid.views

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import de.ur.mi.audidroid.viewmodels.RecordViewModel
import de.ur.mi.audidroid.R
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlinx.android.synthetic.main.record_fragment.*

class RecordFragment : Fragment() {

    var isRecording = false

    companion object {
        fun newInstance() = RecordFragment()
    }

    private lateinit var viewModel: RecordViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.record_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RecordViewModel::class.java)
        initializeRecorder()
    }

    private fun initializeRecorder(){
        viewModel.initializeRecorder(context!!)
        record_pause_button.setOnClickListener{
            viewModel.recordPauseButtonClicked(record_pause_button)
            if(!isRecording){toggleVisibility()}
            isRecording = true
        }
        confirm_button.setOnClickListener {
            viewModel.confirmRecord(context!!)
            viewModel.initializeRecorder(context!!)
            record_pause_button.setImageResource(R.mipmap.record_button_foreground)
            toggleVisibility()
            isRecording = false
        }
        cancel_button.setOnClickListener {
            viewModel.cancelRecord(context!!)
            viewModel.initializeRecorder(context!!)
            record_pause_button.setImageResource(R.mipmap.record_button_foreground)
            toggleVisibility()
            isRecording = false
        }
        toggleVisibility()
    }

    private fun toggleVisibility(){
        confirm_button.visibility = if (confirm_button.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
        cancel_button.visibility = if (cancel_button.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
    }
}
