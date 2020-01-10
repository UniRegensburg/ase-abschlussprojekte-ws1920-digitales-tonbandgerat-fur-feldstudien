package de.ur.mi.audidroid.views


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.ur.mi.audidroid.databinding.ReplayFragmentBinding
import androidx.databinding.DataBindingUtil
import de.ur.mi.audidroid.R

class ReplayFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: ReplayFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.replay_fragment, container, false)

        return binding.root
    }


}
