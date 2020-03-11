package de.ur.mi.audidroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.databinding.MarkItemBinding
import de.ur.mi.audidroid.models.MarkAndTimestamp
import de.ur.mi.audidroid.models.MarkTimestamp

class MarkItemAdapter :
    ListAdapter<MarkAndTimestamp, MarkItemAdapter.ViewHolder>(MarkAndTimeStampDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent) as ViewHolder
    }

    class ViewHolder private constructor(private val binding: MarkItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MarkAndTimestamp
        ) {
            binding.mark = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = MarkItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class MarkAndTimeStampDiffCallback : DiffUtil.ItemCallback<MarkAndTimestamp>() {

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
