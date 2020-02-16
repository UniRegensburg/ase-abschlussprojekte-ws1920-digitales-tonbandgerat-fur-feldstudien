package de.ur.mi.audidroid.views

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.databinding.EditFragmentBinding
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.viewmodels.EditViewModel

class EditFragment : Fragment() {

    private lateinit var binding: EditFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.edit_fragment, container, false)

        val application = this.activity!!.application
        val args = EditFragmentArgs.fromBundle(arguments!!)

        val dataSource = Repository(application)
        val viewModelFactory = EditViewModelFactory(args.recordingPath, dataSource, application)

        val editViewModel = ViewModelProvider(this, viewModelFactory).get(EditViewModel::class.java)

        binding.editViewModel = editViewModel

        binding.setLifecycleOwner(this)

        return binding.root
    }

    /**
     * Provides the Repository, recordingPath and context to the EditViewModel.
     */
    class EditViewModelFactory(
        private val recordingPath: String,
        private val dataSource: Repository,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditViewModel::class.java)) {
                return EditViewModel(recordingPath, dataSource, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
