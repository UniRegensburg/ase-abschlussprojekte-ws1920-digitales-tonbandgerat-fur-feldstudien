package de.ur.mi.audidroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.databinding.EditMarkItemBinding
import de.ur.mi.audidroid.models.MarkerTimeRelation
import de.ur.mi.audidroid.viewmodels.EditRecordingViewModel

class EditMarkerItemAdapter(
    private val editRecordingViewModel: EditRecordingViewModel
) :
    ListAdapter<MarkerTimeRelation, EditMarkerItemAdapter.ViewHolder>(EditMarkDiffCallback()) {

    val userActionsListener = object : EditMarkUserActionsListener {
        override fun onMarkClicked(markerEntity: MarkerTimeRelation) {
            editRecordingViewModel.onMarkClicked(markerEntity.markTime)
        }

        override fun onMarkDeleteClicked(markerEntity: MarkerTimeRelation) {
            editRecordingViewModel.deleteMark(markerEntity.mid)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, userActionsListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent) as ViewHolder
    }

    class ViewHolder private constructor(private val binding: EditMarkItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MarkerTimeRelation,
            listener: EditMarkUserActionsListener
        ) {
            binding.mark = item
            binding.listener = listener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = EditMarkItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class EditMarkDiffCallback : DiffUtil.ItemCallback<MarkerTimeRelation>() {

    override fun areItemsTheSame(
        oldItem: MarkerTimeRelation,
        newItem: MarkerTimeRelation
    ): Boolean {
        return oldItem.mid == newItem.mid
    }

    override fun areContentsTheSame(
        oldItem: MarkerTimeRelation,
        newItem: MarkerTimeRelation
    ): Boolean {
        return oldItem == newItem
    }
}
