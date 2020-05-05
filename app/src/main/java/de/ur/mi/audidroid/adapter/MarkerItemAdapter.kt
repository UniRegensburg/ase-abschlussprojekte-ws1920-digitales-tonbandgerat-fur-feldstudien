package de.ur.mi.audidroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.databinding.MarkerItemBinding
import de.ur.mi.audidroid.models.MarkerEntity
import de.ur.mi.audidroid.viewmodels.EditMarkersViewModel
import de.ur.mi.audidroid.views.EditMarkersFragment

/**
 * Adapter for the [RecyclerView] in [EditMarkersFragment].
 * The adapter connects the data to the RecyclerView.
 * Single Markers get adapted to be displayed in a ViewHolder.
 * Implements a listener for click events on single marker items.
 * @author: Jonas Puchinger
 */

class MarkerItemAdapter(private val editMarkersViewModel: EditMarkersViewModel) :
    ListAdapter<MarkerEntity, MarkerItemAdapter.ViewHolder>(MarkerDiffCallback()) {

    private val userActionsListener = object : MarkerUserActionsListener {

        override fun onMarkerClicked(markerEntity: MarkerEntity) {
            editMarkersViewModel.onMarkerClicked(markerEntity)
        }

        override fun onMarkerDeleteClicked(markerEntity: MarkerEntity) {
            editMarkersViewModel.onMarkerDeleteClicked(markerEntity)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, userActionsListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent) as ViewHolder
    }

    class ViewHolder private constructor(private val binding: MarkerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MarkerEntity, listener: MarkerUserActionsListener) {
            binding.marker = item
            binding.listener = listener
            binding.executePendingBindings()
        }

        companion object {

            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
                val binding: MarkerItemBinding =
                    MarkerItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class MarkerDiffCallback : DiffUtil.ItemCallback<MarkerEntity>() {

    override fun areItemsTheSame(oldItem: MarkerEntity, newItem: MarkerEntity): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: MarkerEntity, newItem: MarkerEntity): Boolean {
        return oldItem == newItem
    }
}
