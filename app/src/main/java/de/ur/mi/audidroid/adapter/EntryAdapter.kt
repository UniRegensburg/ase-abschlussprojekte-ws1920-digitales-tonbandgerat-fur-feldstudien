package de.ur.mi.audidroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import kotlinx.android.synthetic.main.entry_item.view.*

class EntryAdapter: RecyclerView.Adapter<EntryAdapter.EntryViewHolder>(){

    private var entries: List<EntryEntity> = ArrayList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EntryViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.entry_item, parent, false)
        return EntryViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return entries.size
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val currentEntry: EntryEntity = entries[position]
        holder.textViewDate.text = currentEntry.date
    }

    inner class EntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var textViewDate: TextView = itemView.text_recording_date
    }

    internal fun setEntries(entryEntity: List<EntryEntity>){
        this.entries = entryEntity
        notifyDataSetChanged()
    }
}