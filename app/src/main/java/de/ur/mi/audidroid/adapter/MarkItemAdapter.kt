package de.ur.mi.audidroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.databinding.MarkItemBinding
import de.ur.mi.audidroid.models.MarkerTimeRelation

class MarkItemAdapter :
    ListAdapter<MarkerTimeRelation, MarkItemAdapter.ViewHolder>(MarkDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent) as ViewHolder
    }

    class ViewHolder private constructor(private val binding: MarkItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MarkerTimeRelation
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

class MarkDiffCallback : DiffUtil.ItemCallback<MarkerTimeRelation>() {

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
