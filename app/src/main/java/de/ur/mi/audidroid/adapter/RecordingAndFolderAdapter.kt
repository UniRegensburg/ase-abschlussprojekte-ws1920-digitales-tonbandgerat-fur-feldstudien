package de.ur.mi.audidroid.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.models.RecordingAndLabels
import de.ur.mi.audidroid.views.FilesFragment

/**
 * Adapter for the [RecyclerView] in [FilesFragment].
 * The adapter connects the data to the RecyclerView. It adapts the data so that it
 * can be displayed in a ViewHolder. Recordings and folders were handled separately.
 * @author: Theresa Strohmeier, Sabine Roth
 */

class RecordingAndFolderAdapter(
    private val context: Context,
    completeList: MutableList<Any>
) :
    RecyclerView.Adapter<RecordingAndFolderAdapter.BaseViewHolder<*>>() {
    private var adapterDataList = completeList

    companion object {
        private const val TYPE_RECORDING = 0
        private const val TYPE_FOLDER = 1
    }

    inner class RecordingViewHolder(itemView: View) : BaseViewHolder<RecordingAndLabels>(itemView) {

        override fun bind(item: RecordingAndLabels) {
            //TODO: Binding?!
        }
    }

    inner class FolderViewHolder(itemView: View) : BaseViewHolder<FolderEntity>(itemView) {

        override fun bind(item: FolderEntity) {
            //TODO: Binding?!
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            TYPE_RECORDING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recording_item, parent, false)
                RecordingViewHolder(view)
            }
            TYPE_FOLDER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.folder_item, parent, false)
                FolderViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }

    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = adapterDataList[position]
        when (holder) {
            is RecordingViewHolder -> holder.bind(element as RecordingAndLabels)
            is FolderViewHolder -> holder.bind(element as FolderEntity)
            else -> throw IllegalArgumentException()
        }
    }


    /* override fun getItemViewType(position: Int): Int {
         val comparable = data[position]
         return when (comparable) {
             is String -> TYPE_FAMILY
             is Trailer -> TYPE_FRIEND
             is Review -> TYPE_COLLEAGUE
             else -> throw IllegalArgumentException("Invalid type of data " + position)
         }
     }
 */

    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }
}

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

class FolderDiffCallback : DiffUtil.ItemCallback<FolderEntity>() {

    override fun areItemsTheSame(
        oldItem: FolderEntity,
        newItem: FolderEntity
    ): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(
        oldItem: FolderEntity,
        newItem: FolderEntity
    ): Boolean {
        return oldItem == newItem
    }
}
