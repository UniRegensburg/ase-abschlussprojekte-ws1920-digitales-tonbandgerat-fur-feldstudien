package de.ur.mi.audidroid.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.databinding.FolderItemBinding
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.viewmodels.FilesViewModel
import de.ur.mi.audidroid.viewmodels.FolderViewModel
import de.ur.mi.audidroid.views.FilesFragment

class ExternalFolderAdapter(
    private val filesFragment: FilesFragment,
    private val filesViewModel: FilesViewModel,
    private val folderViewModel: FolderViewModel) :
    ListAdapter<FolderEntity, ExternalFolderAdapter.ViewHolder>(ExternalFolderDiffCallback()) {

    private lateinit var holderContext: Context
    private lateinit var recordingAdapter: RecordingItemAdapter
    private lateinit var folderItem: FolderEntity

    private val folderUserActionsListener = object : FolderUserActionsListener{
        override fun onAddFolderClicked(folderEntity: FolderEntity?, view: View) {
            folderViewModel.onAddFolderClicked(folderEntity)
        }

        override fun openFolderPopupMenu(folderEntity: FolderEntity, view: View) {
            filesFragment.openFolderPopupMenu(folderEntity, view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        folderItem = getItem(position)
        setUpRecordingAdapter(holder)
        holder.bind(getItem(position)!!, recordingAdapter, folderUserActionsListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        holderContext = parent.context
        return ViewHolder.from(parent) as ViewHolder
    }

    class ViewHolder private constructor(private val binding: FolderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: FolderEntity,
            adapter: RecordingItemAdapter,
            listener: FolderUserActionsListener
        ) {
            binding.folder = item
            binding.recordingAdapter = adapter
            binding.listener = listener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FolderItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    //Adapter, which is provided for the nested recyclerview
    private fun setUpRecordingAdapter(holder: ViewHolder) {
        val filesViewModel = filesViewModel
        val recordings = filesViewModel.getAllRecordingsByFolder(folderItem)

        recordingAdapter = RecordingItemAdapter(filesFragment, filesViewModel)

        recordings.observe( holder.itemView.context as LifecycleOwner, Observer {
            it?.let {
                //recordingAdapter.submitList(it)
            }
        })

    }
}

class ExternalFolderDiffCallback : DiffUtil.ItemCallback<FolderEntity>() {

    override fun areItemsTheSame(oldItem: FolderEntity, newItem: FolderEntity): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: FolderEntity, newItem: FolderEntity): Boolean {
        return oldItem == newItem
    }
}
