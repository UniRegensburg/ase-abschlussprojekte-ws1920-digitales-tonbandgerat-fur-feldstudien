package de.ur.mi.audidroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.databinding.FolderItemBinding
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.models.RecordingAndLabels
import de.ur.mi.audidroid.views.FilesFragment
import de.ur.mi.audidroid.databinding.RecordingItemBinding
import de.ur.mi.audidroid.viewmodels.FilesViewModel

/**
 * Adapter for the [RecyclerView] in [FilesFragment].
 * The adapter connects the data to the RecyclerView. It adapts the data so that it
 * can be displayed in a ViewHolder. Recordings and folders were handled separately.
 * @author: Theresa Strohmeier, Sabine Roth
 */

class RecordingAndFolderAdapter(
    listener: RecordingAndFolderActionsListener
) :
    ListAdapter<Any, RecordingAndFolderAdapter.BaseViewHolder<*>>(RecordingAndFolderDiffCallback()) {

    companion object {
        private const val TYPE_RECORDING = 0
        private const val TYPE_FOLDER = 1
    }

    private val userActionsListener = listener

    class RecordingViewHolder(private val binding: RecordingItemBinding) : BaseViewHolder<RecordingAndLabels>(binding.root) {

        override fun bind(
            item: RecordingAndLabels,
            listener: RecordingAndFolderActionsListener
        ) {
            binding.recording = item
            binding.listener = listener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
                val binding: RecordingItemBinding =
                    RecordingItemBinding.inflate(layoutInflater, parent, false)
                return RecordingViewHolder(binding)
            }
        }
    }

    class FolderViewHolder(private val binding: FolderItemBinding) : BaseViewHolder<FolderEntity>(binding.root) {

        override fun bind(
            item: FolderEntity,
            listener: RecordingAndFolderActionsListener
        ) {
            binding.folder = item
            binding.listener = listener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
                val binding: FolderItemBinding =
                    FolderItemBinding.inflate(layoutInflater, parent, false)
                return FolderViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            TYPE_RECORDING -> {
                RecordingViewHolder.from(parent) as RecordingViewHolder
            }
            TYPE_FOLDER -> {
                FolderViewHolder.from(parent) as FolderViewHolder
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }

    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = getItem(position)
        when (holder) {
            is RecordingViewHolder -> holder.bind(element as RecordingAndLabels, userActionsListener)
            is FolderViewHolder -> holder.bind(element as FolderEntity, userActionsListener)
            else -> throw IllegalArgumentException()
        }
    }

    abstract class BaseViewHolder<T>(item: View) : RecyclerView.ViewHolder(item) {
        abstract fun bind(item: T, listener: RecordingAndFolderActionsListener)
    }
}

class RecordingAndFolderDiffCallback : DiffUtil.ItemCallback<Any>() {

    override fun areItemsTheSame(
        oldItem: Any,
        newItem: Any
    ): Boolean {
        var itemsAreSame = false
        if (oldItem is RecordingAndLabels && newItem is RecordingAndLabels) {
            itemsAreSame = oldItem.uid == newItem.uid
        } else if (oldItem is FolderEntity && newItem is FolderEntity) {
            itemsAreSame = oldItem.uid == newItem.uid
        }
        return itemsAreSame
    }

    /**
     * Cast are needed despite the warning to remove them.
     */
    override fun areContentsTheSame(
        oldItem: Any,
        newItem: Any
    ): Boolean {
        var contentsAreSame = false
        if (oldItem is RecordingAndLabels && newItem is RecordingAndLabels) {
            contentsAreSame = oldItem as RecordingAndLabels == newItem as RecordingAndLabels
        } else if (oldItem is FolderEntity && newItem is FolderEntity) {
            contentsAreSame = oldItem as FolderEntity == newItem as FolderEntity
        }
        return contentsAreSame
    }
}
