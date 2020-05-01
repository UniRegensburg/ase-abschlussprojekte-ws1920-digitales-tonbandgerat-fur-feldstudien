package de.ur.mi.audidroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.databinding.EditMarkItemBinding
import de.ur.mi.audidroid.models.ExpandableMarkAndTimestamp
import de.ur.mi.audidroid.models.MarkAndTimestamp
import de.ur.mi.audidroid.viewmodels.EditRecordingViewModel

class EditMarkerItemAdapter(
    private val editRecordingViewModel: EditRecordingViewModel
) :
    ListAdapter<MarkAndTimestamp, EditMarkerItemAdapter.ViewHolder>(EditMarkAndTimeStampDiffCallback()) {

    private val userActionsListener = object : EditMarkUserActionsListener {
        override fun onMarkClicked(mark: ExpandableMarkAndTimestamp, view: View) {
            if (mark.markAndTimestamp.markTimestamp.markComment != null) {
                mark.isExpanded = !mark.isExpanded
            }
        }

        override fun onEditCommentClicked(mark: ExpandableMarkAndTimestamp, view: View) {
            editRecordingViewModel.onEditCommentClicked(mark)
        }

        override fun onMarkDeleteClicked(mark: MarkAndTimestamp) {
            editRecordingViewModel.onMarkDeleteClicked(mark)
        }

        override fun onMarkTimeClicked(mark: MarkAndTimestamp) {
            editRecordingViewModel.onMarkTimeClicked(mark.markTimestamp.markTimeInMilli)
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
            binding.mark = ExpandableMarkAndTimestamp(item)
            binding.listener = listener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
                val binding: EditMarkItemBinding =
                    EditMarkItemBinding.inflate(layoutInflater, parent, false)
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
