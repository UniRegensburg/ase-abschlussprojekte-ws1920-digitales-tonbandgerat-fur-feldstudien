package de.ur.mi.audidroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.databinding.EditMarkItemBinding
import de.ur.mi.audidroid.models.MarkTimestamp
import de.ur.mi.audidroid.viewmodels.EditRecordingViewModel

class EditMarkerItemAdapter(
    private val editRecordingViewModel: EditRecordingViewModel
) :
    ListAdapter<MarkTimestamp, EditMarkerItemAdapter.ViewHolder>(EditMarkDiffCallback()) {

    private val userActionsListener = object : EditMarkUserActionsListener {
        override fun onMarkClicked(markerEntity: MarkTimestamp) {
            editRecordingViewModel.onMarkClicked(markerEntity.markTime)
        }

        override fun onMarkDeleteClicked(markerEntity: MarkTimestamp) {
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
            item: MarkTimestamp,
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

class EditMarkDiffCallback : DiffUtil.ItemCallback<MarkTimestamp>() {

    override fun areItemsTheSame(
        oldItem: MarkTimestamp,
        newItem: MarkTimestamp
    ): Boolean {
        return oldItem.mid == newItem.mid
    }

    override fun areContentsTheSame(
        oldItem: MarkTimestamp,
        newItem: MarkTimestamp
    ): Boolean {
        return oldItem == newItem
    }
}
