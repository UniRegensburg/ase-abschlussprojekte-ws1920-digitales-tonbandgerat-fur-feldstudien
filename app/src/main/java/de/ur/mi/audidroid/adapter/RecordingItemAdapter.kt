package de.ur.mi.audidroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.databinding.RecordingItemBinding
import de.ur.mi.audidroid.models.RecordingAndLabels
import de.ur.mi.audidroid.viewmodels.FilesViewModel
import de.ur.mi.audidroid.views.FilesFragment

/**
 * Adapter for the [RecyclerView] in [FilesFragment].
 * The adapter connects the data to the RecyclerView. It adapts the data so that it
 * can be displayed in a ViewHolder.
 * @author: Theresa Strohmeier
 */

class RecordingItemAdapter(
    private val filesFragment: FilesFragment,
    private val filesViewModel: FilesViewModel
) :
    ListAdapter<RecordingAndLabels, RecordingItemAdapter.ViewHolder>(RecordingDiffCallback()) {

    val userActionsListener = object : RecordingUserActionsListener {
        override fun onRecordingClicked(recordingAndLabels: RecordingAndLabels) {
            filesViewModel.onRecordingClicked(
                recordingAndLabels.uid,
                recordingAndLabels.recordingName,
                recordingAndLabels.recordingPath
            )
        }

        override fun onButtonClicked(recordingAndLabels: RecordingAndLabels, view: View) {
            filesFragment.openPopupMenu(recordingAndLabels, view)
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
            item: RecordingAndLabels,
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
class RecordingDiffCallback : DiffUtil.ItemCallback<RecordingAndLabels>() {

    override fun areItemsTheSame(
        oldItem: RecordingAndLabels,
        newItem: RecordingAndLabels
    ): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(
        oldItem: RecordingAndLabels,
        newItem: RecordingAndLabels
    ): Boolean {
        return oldItem == newItem
    }
}
