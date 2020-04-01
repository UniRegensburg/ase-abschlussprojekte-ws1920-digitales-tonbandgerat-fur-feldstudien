package de.ur.mi.audidroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.databinding.MarkerButtonBinding
import de.ur.mi.audidroid.models.MarkerEntity
import de.ur.mi.audidroid.viewmodels.RecordViewModel

class MarkerButtonAdapter(
    private val recordViewModel: RecordViewModel
) : ListAdapter<MarkerEntity, MarkerButtonAdapter.ViewHolder>(MarkerDiffCallback()) {

    private val markerButtonUserActionsListener = object : MarkerButtonUserActionsListener {

        override fun onMarkerButtonClicked(
            markerEntity: MarkerEntity
        ) {
            recordViewModel.onMarkerButtonClicked(markerEntity)
        }
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position) as MarkerEntity, markerButtonUserActionsListener)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder.from(parent) as ViewHolder
    }

    class ViewHolder private constructor(
        private val binding: MarkerButtonBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MarkerEntity,
            listener: MarkerButtonUserActionsListener
        ) {
            binding.marker = item
            binding.listener = listener
            binding.executePendingBindings()
        }

        companion object {

            fun from(
                parent: ViewGroup
            ): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = MarkerButtonBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}
