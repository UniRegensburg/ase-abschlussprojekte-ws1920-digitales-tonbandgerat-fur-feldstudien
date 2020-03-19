package de.ur.mi.audidroid.views

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.adapter.MarkerButtonAdapter
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
    private lateinit var dataSource: Repository
    var orientationEventListener: OrientationEventListener? = null

    companion object {
        fun newInstance() = RecordFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        addRotationListener()
        val application = this.activity!!.application
        dataSource = Repository(application)
        binding = DataBindingUtil.inflate(inflater, R.layout.record_fragment, container, false)

        val viewModelFactory = RecordViewModelFactory(dataSource, application, context!!)
        viewModel = ViewModelProvider(this, viewModelFactory).get(RecordViewModel::class.java)

        binding.recordViewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    class RecordViewModelFactory(
        private val dataSource: Repository,
        private val application: Application,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(RecordViewModel::class.java)) {
                RecordViewModel(dataSource, application, context) as T
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
                de.ur.mi.audidroid.utils.SaveRecordingDialog.createDialog(
                    paramContext = context!!,
                    layoutId = R.layout.save_dialog,
                    errorMessage = viewModel.errorMessage,
                    dataSource = dataSource,
                    recordViewModel = viewModel,
                    recordFragment = this

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

    private fun addRotationListener() {
        if (getRotationPreference()) {
            activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            orientationEventListener = object : OrientationEventListener(activity) {
                override fun onOrientationChanged(orientation: Int) {
                    if (orientation in 70..290) activity!!.requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    else if (orientation in 291..360 || (orientation in 0..69)) activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
            orientationEventListener!!.enable()
        } else {
            this.activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            orientationEventListener?.disable()
        }
    }

    private fun getRotationPreference(): Boolean {
        return context!!.getSharedPreferences(
            activity!!.resources.getString(R.string.rotation_preference_key),
            Context.MODE_PRIVATE
        ).getBoolean(activity!!.resources.getString(R.string.rotation_preference_key), true)
    }

    override fun onPause() {
        super.onPause()
        viewModel.fragmentOnPause()

    }

    override fun onStop() {
        super.onStop()
        orientationEventListener?.disable()
        activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onResume() {
        super.onResume()
        addRotationListener()
    }
}
