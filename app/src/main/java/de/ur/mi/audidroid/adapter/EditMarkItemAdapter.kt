package de.ur.mi.audidroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BaseObservable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.databinding.EditMarkItemBinding
import de.ur.mi.audidroid.models.MarkAndTimestamp
import de.ur.mi.audidroid.viewmodels.EditRecordingViewModel

class EditMarkerItemAdapter(
    private val editRecordingViewModel: EditRecordingViewModel
) :
    ListAdapter<MarkAndTimestamp, EditMarkerItemAdapter.ViewHolder>(EditMarkAndTimeStampDiffCallback()) {

    private val userActionsListener = object : EditMarkUserActionsListener {
        override fun onMarkClicked(mark: MarkAndTimestamp, view: View) {
            if (mark.markTimestamp.markComment != null) {
                editRecordingViewModel.onMarkClicked(view)
            }
        }

        override fun onEditCommentClicked(mark: MarkAndTimestamp) {
            editRecordingViewModel.onEditCommentClicked(mark.markTimestamp)
        }

        override fun onMarkDeleteClicked(mark: MarkAndTimestamp) {
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
            item: MarkAndTimestamp,
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

class EditMarkAndTimeStampDiffCallback : DiffUtil.ItemCallback<MarkAndTimestamp>() {

    override fun areItemsTheSame(
        oldItem: MarkAndTimestamp,
        newItem: MarkAndTimestamp
    ): Boolean {
        return oldItem.markTimestamp.mid == newItem.markTimestamp.mid
    }

    override fun areContentsTheSame(
        oldItem: MarkAndTimestamp,
        newItem: MarkAndTimestamp
    ): Boolean {
        return oldItem == newItem
    }
}
