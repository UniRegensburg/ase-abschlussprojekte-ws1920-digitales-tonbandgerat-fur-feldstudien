package de.ur.mi.audidroid.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.utils.Dialog

class LabelAdapter(
    val context: Context,
    var labels: ArrayList<String>, val dialog: Dialog
) : RecyclerView.Adapter<LabelAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.label_item, parent, false)
        return Holder(view, context, dialog)
    }

    override fun getItemCount() = labels.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(labels[position])
    }

    class Holder(private val v: View, val context: Context, val dialog: Dialog) : RecyclerView.ViewHolder(v),
        View.OnClickListener {

        fun bind(label: String) {
            (v as MaterialButton).text = label
            v.setOnClickListener(this)
        }

        override fun onClick(clickedView: View) {
            dialog.labelClicked(clickedView)
        }
    }
}
