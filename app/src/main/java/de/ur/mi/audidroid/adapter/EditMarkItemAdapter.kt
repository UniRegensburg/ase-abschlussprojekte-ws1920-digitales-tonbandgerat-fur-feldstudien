package de.ur.mi.audidroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.databinding.EditMarkItemBinding
import de.ur.mi.audidroid.models.CombinedMarkAndTimestamp
import de.ur.mi.audidroid.models.MarkAndTimestamp
import de.ur.mi.audidroid.models.MarkTimestamp
import de.ur.mi.audidroid.viewmodels.EditRecordingViewModel

class EditMarkerItemAdapter(
    private val editRecordingViewModel: EditRecordingViewModel
) :
    ListAdapter<CombinedMarkAndTimestamp, EditMarkerItemAdapter.ViewHolder>(EditMarkAndTimeStampDiffCallback()) {

    private val userActionsListener = object : EditMarkUserActionsListener {
        override fun onMarkClicked(mark: CombinedMarkAndTimestamp) {
            editRecordingViewModel.onMarkClicked(mark.markTimestamp.markTime)
        }

        override fun onMarkDeleteClicked(mark: CombinedMarkAndTimestamp) {
            editRecordingViewModel.deleteMark(mark.markTimestamp.mid)
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
            item: CombinedMarkAndTimestamp,
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

class EditMarkAndTimeStampDiffCallback : DiffUtil.ItemCallback<CombinedMarkAndTimestamp>() {

    override fun areItemsTheSame(
        oldItem: CombinedMarkAndTimestamp,
        newItem: CombinedMarkAndTimestamp
    ): Boolean {
        return oldItem.markTimestamp.mid == newItem.markTimestamp.mid
    }

    override fun areContentsTheSame(
        oldItem: CombinedMarkAndTimestamp,
        newItem: CombinedMarkAndTimestamp
    ): Boolean {
        return oldItem == newItem
    }
}
