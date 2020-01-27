package de.ur.mi.audidroid.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.viewmodels.FilesViewModel

class FilesFragment : Fragment() {

    companion object {
        fun newInstance() = FilesFragment()
    }

    private lateinit var viewModel: FilesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.files_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FilesViewModel::class.java)
        // TODO: Use the ViewModel
    }
}
