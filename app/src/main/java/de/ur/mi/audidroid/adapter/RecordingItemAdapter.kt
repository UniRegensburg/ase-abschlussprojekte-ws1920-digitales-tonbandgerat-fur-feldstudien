package de.ur.mi.audidroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.databinding.RecordingItemBinding
import de.ur.mi.audidroid.viewmodels.FilesViewModel

/**
 * Adapter for the [RecyclerView] in [FilesFragment].
 * The adapter connects the data to the RecyclerView. It adapts the data so that it
 * can be displayed in a ViewHolder.
 * @author: Theresa Strohmeier
 */
class RecordingItemAdapter(private val filesViewModel: FilesViewModel) :
    ListAdapter<EntryEntity, RecordingItemAdapter.ViewHolder>(RecordingDiffCallback()) {

    val userActionsListener = object : RecordingUserActionsListener {
        override fun onRecordingClicked(entryEntity: EntryEntity) {
            filesViewModel.onRecordingClicked(entryEntity.uid)
        }

        override fun onButtonClicked(entryEntity: EntryEntity, view: View) {
            filesViewModel.onButtonClicked(entryEntity, view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, userActionsListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent) as ViewHolder
    }

    class ViewHolder private constructor(private val binding: RecordingItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: EntryEntity,
            listener: RecordingUserActionsListener
        ) {
            binding.recording = item
            binding.listener = listener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecordingItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

// DiffUtil uses these two methods to figure out how the list and items have changed
class RecordingDiffCallback : DiffUtil.ItemCallback<EntryEntity>() {

    override fun areItemsTheSame(oldItem: EntryEntity, newItem: EntryEntity): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: EntryEntity, newItem: EntryEntity): Boolean {
        return oldItem == newItem
    }
}
