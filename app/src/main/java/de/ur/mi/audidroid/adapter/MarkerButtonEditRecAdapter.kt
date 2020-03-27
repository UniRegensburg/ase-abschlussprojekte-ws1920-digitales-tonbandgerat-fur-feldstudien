package de.ur.mi.audidroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.databinding.MarkerButtonEditRecBinding
import de.ur.mi.audidroid.models.MarkerEntity
import de.ur.mi.audidroid.viewmodels.EditRecordingViewModel

class MarkerButtonEditRecAdapter(private val editRecordingViewModel: EditRecordingViewModel) :
    ListAdapter<MarkerEntity, MarkerButtonEditRecAdapter.ViewHolder>(MarkerDiffCallback()) {

    private val markerButtonUserActionsListener = object : MarkerButtonUserActionsListener {

        override fun onMarkerButtonClicked(markerEntity: MarkerEntity) {
            editRecordingViewModel.onMarkerButtonClicked(markerEntity)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position) as MarkerEntity, markerButtonUserActionsListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent) as ViewHolder
    }

    class ViewHolder private constructor(private val binding: MarkerButtonEditRecBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MarkerEntity,
            listener: MarkerButtonUserActionsListener
        ) {
            binding.marker = item
            binding.listener = listener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = MarkerButtonEditRecBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}
