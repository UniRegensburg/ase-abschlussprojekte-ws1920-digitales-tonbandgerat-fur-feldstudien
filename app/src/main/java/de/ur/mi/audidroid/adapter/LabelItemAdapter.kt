package de.ur.mi.audidroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.databinding.LabelItemBinding
import de.ur.mi.audidroid.models.LabelEntity
import de.ur.mi.audidroid.viewmodels.EditLabelsViewModel
import de.ur.mi.audidroid.views.EditLabelsFragment

/**
 * Adapter for the [RecyclerView] in [EditLabelsFragment].
 * The adapter connects the data to the RecyclerView.
 * Single Labels get adapted to be displayed in a ViewHolder.
 * Implements a listener for click events on single Label items.
 * @author: Jonas Puchinger
 */

class LabelItemAdapter(private val editLabelsViewModel: EditLabelsViewModel) :
    ListAdapter<LabelEntity, LabelItemAdapter.ViewHolder>(LabelDiffCallback()) {

    private val userActionsListener = object : LabelUserActionsListener {

        override fun onLabelClicked(labelEntity: LabelEntity) {
            editLabelsViewModel.onLabelClicked(labelEntity)
        }

        override fun onLabelDeleteClicked(labelEntity: LabelEntity) {
            editLabelsViewModel.onLabelDeleteClicked(labelEntity)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, userActionsListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent) as ViewHolder
    }

    class ViewHolder private constructor(private val binding: LabelItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LabelEntity, listener: LabelUserActionsListener) {
            binding.label = item
            binding.listener = listener
            binding.executePendingBindings()
        }

        companion object {

            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
                val binding: LabelItemBinding =
                    LabelItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class LabelDiffCallback : DiffUtil.ItemCallback<LabelEntity>() {

    override fun areItemsTheSame(oldItem: LabelEntity, newItem: LabelEntity): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: LabelEntity, newItem: LabelEntity): Boolean {
        return oldItem == newItem
    }
}
