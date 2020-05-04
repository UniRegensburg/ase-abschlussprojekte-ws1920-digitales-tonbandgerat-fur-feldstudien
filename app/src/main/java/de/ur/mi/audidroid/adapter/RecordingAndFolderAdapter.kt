package de.ur.mi.audidroid.adapter

import android.annotation.SuppressLint
import android.app.Application
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.databinding.FolderItemBinding
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.models.RecordingAndLabels
import de.ur.mi.audidroid.views.FilesFragment
import de.ur.mi.audidroid.databinding.RecordingItemBinding
import de.ur.mi.audidroid.viewmodels.FilesViewModel
import kotlinx.android.synthetic.main.folder_item.view.*
import kotlinx.android.synthetic.main.recording_item.view.*

/**
 * Adapter for the [RecyclerView] in [FilesFragment].
 * The adapter connects the data to the RecyclerView. It adapts the data so that it
 * can be displayed in a ViewHolder. Recordings and folders were handled separately.
 * @author: Theresa Strohmeier, Sabine Roth
 */

class RecordingAndFolderAdapter(
    private val context: Context,
    private val filesViewModel: FilesViewModel,
    listener: RecordingAndFolderActionsListener
) :
    ListAdapter<Any, RecordingAndFolderAdapter.BaseViewHolder<*>>(RecordingAndFolderDiffCallback()) {

    companion object {
        private const val TYPE_RECORDING = 0
        private const val TYPE_FOLDER = 1
    }

    private val userActionsListener = listener

    class RecordingViewHolder(private val binding: RecordingItemBinding) :
        BaseViewHolder<RecordingAndLabels>(binding.root) {

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

    class FolderViewHolder(private val binding: FolderItemBinding) :
        BaseViewHolder<FolderEntity>(binding.root) {

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

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is RecordingAndLabels -> 0
            is FolderEntity -> 1
            else -> throw IllegalArgumentException()
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
            is RecordingViewHolder -> {
                holder.bind(element as RecordingAndLabels, userActionsListener)
                bindLongClickListener(holder, element)
            }
            is FolderViewHolder -> {
                holder.bind(element as FolderEntity, userActionsListener)
                bindDragListener(holder, element)
            }
            else -> throw IllegalArgumentException()
        }
    }

    private fun bindLongClickListener(holder: RecordingViewHolder, element: RecordingAndLabels) {
        holder.itemView.recording_card_layout.setOnLongClickListener { v ->
            v.tag = "recording"
            val tagItem = ClipData.Item(v.tag as? CharSequence)
            val elementIdItem = ClipData.Item(element.uid.toString() as? CharSequence)
            val dragData = ClipData(
                v.tag as? CharSequence,
                arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                tagItem
            )
            dragData.addItem(elementIdItem)
            val myShadow = View.DragShadowBuilder(v)
            v.startDragAndDrop(
                dragData,
                myShadow,
                null,
                0
            )
            true
        }
    }

    private fun bindDragListener(holder: FolderViewHolder, element: FolderEntity) {
        holder.itemView.folder_card_layout.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        v.setBackgroundColor(Color.TRANSPARENT)
                        v.invalidate()
                        true
                    } else {
                        false
                    }
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    v.setBackgroundColor(context.getColor(R.color.color_primary))
                    v.invalidate()
                    true
                }
                DragEvent.ACTION_DRAG_LOCATION ->
                    true
                DragEvent.ACTION_DRAG_EXITED -> {
                    v.setBackgroundColor(Color.TRANSPARENT)
                    v.invalidate()
                    true
                }
                DragEvent.ACTION_DROP -> {
                    val recordingId: ClipData.Item = event.clipData.getItemAt(1)
                    filesViewModel.moveRecording(recordingId.text.toString().toInt(), element)
                    v.invalidate()
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    v.setBackgroundColor(Color.TRANSPARENT)
                    v.invalidate()
                    true
                }
                else -> false
            }
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

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(
        oldItem: Any,
        newItem: Any
    ): Boolean {
        var contentsAreSame = false
        if (oldItem is RecordingAndLabels && newItem is RecordingAndLabels) {
            contentsAreSame = oldItem == newItem
        } else if (oldItem is FolderEntity && newItem is FolderEntity) {
            contentsAreSame = oldItem == newItem
        }
        return contentsAreSame
    }
}
