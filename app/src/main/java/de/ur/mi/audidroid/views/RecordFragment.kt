package de.ur.mi.audidroid.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.viewmodels.RecordViewModel
import kotlinx.android.synthetic.main.record_fragment.*
import java.io.File
import java.net.URI

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
        initializeRecorderFunctionality()
    }

    private fun initializeRecorderFunctionality() {
        viewModel.initializeTimer(chronometer)
        record_pause_button.setOnClickListener {
            viewModel.recordPauseButtonClicked(record_pause_button, context!!)
            if (!isRecording) {
                toggleVisibility()
            }
            isRecording = true
        }
        confirm_button.setOnClickListener {
            viewModel.confirmRecord(context!!)
            record_pause_button.setImageResource(R.mipmap.record_button_foreground)
            toggleVisibility()
            isRecording = false
        }
        cancel_button.setOnClickListener {
            viewModel.cancelRecord(context!!)
            record_pause_button.setImageResource(R.mipmap.record_button_foreground)
            toggleVisibility()
            isRecording = false
        }
        toggleVisibility()
    }


    private fun toggleVisibility() {
        confirm_button.visibility =
            if (confirm_button.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
        cancel_button.visibility =
            if (cancel_button.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
    }
}
