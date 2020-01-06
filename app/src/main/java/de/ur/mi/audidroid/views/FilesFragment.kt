package de.ur.mi.audidroid.views

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import de.ur.mi.audidroid.adapter.EntryAdapter
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity

import de.ur.mi.audidroid.viewmodels.FilesViewModel
import kotlinx.android.synthetic.main.files_fragment.*

class FilesFragment : Fragment() {

    companion object {
        fun newInstance() = FilesFragment()
    }

    private lateinit var viewModel: FilesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.files_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FilesViewModel::class.java)

        recycler_view.layoutManager = LinearLayoutManager(context)
        var adapter = EntryAdapter()
        recycler_view.adapter = adapter

        viewModel = ViewModelProviders.of(this).get(FilesViewModel::class.java)
        viewModel.getAllRecordings().observe(this, Observer<List<EntryEntity>> {
            adapter.setEntries(it)
        })
    }
}
