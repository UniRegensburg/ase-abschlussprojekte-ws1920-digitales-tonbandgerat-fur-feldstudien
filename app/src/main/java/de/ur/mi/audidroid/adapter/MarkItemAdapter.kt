package de.ur.mi.audidroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.databinding.MarkItemBinding
import de.ur.mi.audidroid.models.ExpandableMarkAndTimestamp
import de.ur.mi.audidroid.models.MarkAndTimestamp
import de.ur.mi.audidroid.views.PlayerFragment

/**
 * Adapter for the [RecyclerView] in [PlayerFragment].
 * The adapter connects the data to the RecyclerView.
 * Single Marks get adapted to be displayed in a ViewHolder.
 * Implements a listener for click events on single Mark items.
 * @author: Theresa Strohmeier, Jonas Puchinger
 */

class MarkItemAdapter :
    ListAdapter<MarkAndTimestamp, MarkItemAdapter.ViewHolder>(MarkAndTimeStampDiffCallback()) {

    private val userActionsListener = object : MarkUserActionsListener {

        override fun onMarkClicked(mark: ExpandableMarkAndTimestamp, view: View) {
            if (mark.markAndTimestamp.markTimestamp.markComment != null) {
                mark.isExpanded = !mark.isExpanded
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, userActionsListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent) as ViewHolder
    }

    class ViewHolder private constructor(private val binding: MarkItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MarkAndTimestamp, listener: MarkUserActionsListener) {
            binding.mark = ExpandableMarkAndTimestamp(item)
            binding.listener = listener
            binding.executePendingBindings()
        }

        companion object {

            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
                val binding: MarkItemBinding =
                    MarkItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class MarkAndTimeStampDiffCallback : DiffUtil.ItemCallback<MarkAndTimestamp>() {

    override fun areItemsTheSame(oldItem: MarkAndTimestamp, newItem: MarkAndTimestamp): Boolean {
        return oldItem.markTimestamp.mid == newItem.markTimestamp.mid
    }

    override fun areContentsTheSame(oldItem: MarkAndTimestamp, newItem: MarkAndTimestamp): Boolean {
        return oldItem == newItem
    }
}
