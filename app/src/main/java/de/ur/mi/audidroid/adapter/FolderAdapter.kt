package de.ur.mi.audidroid.adapter

import android.content.Context
import android.telecom.RemoteConference
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.databinding.FolderItemBinding
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.models.RecordingAndLabels
import de.ur.mi.audidroid.viewmodels.FilesViewModel
import de.ur.mi.audidroid.viewmodels.FolderViewModel
import de.ur.mi.audidroid.views.FilesFragment

class FolderAdapter(
    private val filesFragment: FilesFragment,
    private val filesViewModel: FilesViewModel,
    private val folderViewModel: FolderViewModel) :
    ListAdapter<FolderEntity, FolderAdapter.ViewHolder>(FolderDiffCallback()) {

    private lateinit var holderContext: Context
    private lateinit var recordingAdapter: RecordingItemAdapter
    private lateinit var folderItem: FolderEntity
    private var isSubfolder: Boolean = false
    private lateinit var folderToBeUpdated: FolderEntity
    private var _isExpanded = MutableLiveData<Boolean>()
    val isExpanded: LiveData<Boolean>
        get() = _isExpanded

    private var _updateContent = MutableLiveData<Boolean>()
    val updateContent: LiveData<Boolean>
        get() = _updateContent

    private val folderUserActionsListener = object : FolderUserActionsListener {
        override fun onAddFolderClicked(folderEntity: FolderEntity?, view: View) {
            folderViewModel.onAddFolderClicked(folderEntity)
        }

        override fun openFolderPopupMenu(folderEntity: FolderEntity, view: View) {
            filesFragment.openFolderPopupMenu(folderEntity, view)
        }

        override fun changeFolderExpansion(folderEntity: FolderEntity, view: View) {
            println("Expansion wird geändert")
            folderToBeUpdated = folderEntity
            _isExpanded.value = !isExpanded.value!!
            println(isExpanded.value!!)
            println("______________________________")
            folderViewModel.invalidateFolderView(view)
        }

    }
/*External Folder Adapter
 val recordings = filesViewModel.getAllRecordingsByFolder(folderItem)
        recordingAdapter = RecordingItemAdapter(filesFragment, filesViewModel)
        recordings.observe( holder.itemView.context as LifecycleOwner, Observer {
            it?.let {
                //recordingAdapter.submitList(it)
            }
        })
* */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        folderItem = getItem(position)
        isSubfolder = folderViewModel.isSubfolder(folderItem)
        _isExpanded.value = true
        _updateContent.value = true
        setUpRecordingAdapter(holder)
        holder.bind(getItem(position)!!, recordingAdapter, folderUserActionsListener, isSubfolder)
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
            listener: FolderUserActionsListener,
            boolean: Boolean
        ) {
            binding.folder = item
            binding.recordingAdapter = adapter
            binding.listener = listener
            binding.isSubfolder = boolean
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
        recordingAdapter = RecordingItemAdapter(filesFragment, filesViewModel)
        val recordingsWithLabels = filesViewModel.allRecordingsWithLabels
        val recordingList = mutableListOf<RecordingAndLabels>()

        recordingsWithLabels.observe(holder.itemView.context as LifecycleOwner, Observer {
            //println("RECORDINGS WITH LABEls")
            recordingsWithLabels.value!!.forEach {
                if (it.folder == folderItem.uid){recordingList.add(it)}
            }
            //println(updateContent.value)
            _updateContent.value = !_updateContent.value!!
            // println(updateContent.value)
        })

        isExpanded.observe(holder.itemView.context as LifecycleOwner, Observer {
            //  println("UPDATE CONTENT ")
            //   println(folderItem)
            // println(updateContent.value)
            _updateContent.value = !_updateContent.value!!
            // println(updateContent.value)
        })

        updateContent.observe(holder.itemView.context as LifecycleOwner, Observer {
            // println("INHALT MUSS GEÄNDERT WERDEN")
            if (isExpanded.value!!){
                //  println("IST AUSGEKLAPPT: "+ recordingList)

                recordingAdapter.submitList(recordingList)

            }else{
                val empty = listOf<RecordingAndLabels>()
                // println("IST EINGEKLAPPT: "+ empty)

                recordingAdapter.submitList(empty)

            }
        })
        /*
        recordingsWithLabels.observe(holder.itemView.context as LifecycleOwner, Observer {
            println("hier")
            recordingsWithLabels.value!!.forEach {
                if (it.folder == folderItem.uid){recordingList.add(it)} }
            recordingAdapter.submitList(recordingList)
        })
        */

    }
}

class FolderDiffCallback : DiffUtil.ItemCallback<FolderEntity>() {

    override fun areItemsTheSame(oldItem: FolderEntity, newItem: FolderEntity): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: FolderEntity, newItem: FolderEntity): Boolean {
        return oldItem == newItem
    }
}
