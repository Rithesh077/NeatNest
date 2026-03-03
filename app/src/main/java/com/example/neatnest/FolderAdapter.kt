package com.example.neatnest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

// adapter for folder cards in the digital asset hub
class FolderAdapter(
    private val onFolderClick: (String) -> Unit
) : ListAdapter<Pair<String, Int>, FolderAdapter.FolderViewHolder>(FolderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_folder_card, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val (category, count) = getItem(position)
        holder.bind(category, count)
    }

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val tvFileCount: TextView = itemView.findViewById(R.id.tvFileCount)
        private val ivFolderIcon: ImageView = itemView.findViewById(R.id.ivFolderIcon)

        fun bind(category: String, count: Int) {
            tvCategoryName.text = category
            tvFileCount.text = "$count ${if (count == 1) "file" else "files"}"

            // set tint based on category
            val tintColor = when (category) {
                "Study Material" -> R.color.organizer_tint
                "Work Documents" -> R.color.utility_tint
                "Media" -> R.color.devmode_tint
                "Clutter" -> R.color.priority_high
                else -> R.color.text_secondary
            }
            ivFolderIcon.setColorFilter(itemView.context.getColor(tintColor))

            itemView.setOnClickListener { onFolderClick(category) }
        }
    }

    class FolderDiffCallback : DiffUtil.ItemCallback<Pair<String, Int>>() {
        override fun areItemsTheSame(oldItem: Pair<String, Int>, newItem: Pair<String, Int>): Boolean {
            return oldItem.first == newItem.first
        }

        override fun areContentsTheSame(oldItem: Pair<String, Int>, newItem: Pair<String, Int>): Boolean {
            return oldItem == newItem
        }
    }
}
