package de.ur.mi.audidroid.views


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import de.ur.mi.audidroid.databinding.ReplayFragmentBinding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryRepository
import de.ur.mi.audidroid.viewmodels.ReplayViewModel
import java.lang.IllegalArgumentException

class ReplayFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: ReplayFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.replay_fragment, container, false)

        val application = requireNotNull(this.activity).application

        val args = ReplayFragmentArgs.fromBundle(arguments!!)
        Toast.makeText(context, "RecordingId: ${args.recordingPath}", Toast.LENGTH_SHORT).show()

        val dataSource = EntryRepository(application)
        val viewModelFactory = ReplayViewModelFactory(args.recordingPath, dataSource)

        val replayViewModel = ViewModelProviders.of(this, viewModelFactory).get(ReplayViewModel::class.java)

        binding.replayViewModel = replayViewModel

        binding.setLifecycleOwner(this)

        return binding.root
    }

    class ReplayViewModelFactory(private val recordingPath: String, private val dataSource: EntryRepository) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T: ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(ReplayViewModel::class.java)) {
                return ReplayViewModel(recordingPath, dataSource) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
