package de.ur.mi.audidroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.databinding.EntryItemBinding

class EntryAdapter: ListAdapter<EntryEntity, EntryAdapter.ViewHolder>(RecordingDiffCallback()){

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent) as ViewHolder
    }

    class ViewHolder private constructor(val binding: EntryItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: EntryEntity) {
            binding.recording = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = EntryItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class RecordingDiffCallback : DiffUtil.ItemCallback<EntryEntity>() {

    override fun areItemsTheSame(oldItem: EntryEntity, newItem: EntryEntity): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: EntryEntity, newItem: EntryEntity): Boolean {
        return oldItem == newItem
    }
}