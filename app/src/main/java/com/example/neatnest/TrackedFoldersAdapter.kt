package com.example.neatnest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class TrackedFoldersAdapter(private val onRemoveClicked: (TrackedFolder) -> Unit) :
    ListAdapter<TrackedFolder, TrackedFoldersAdapter.FolderViewHolder>(FolderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tracked_folder, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = getItem(position)
        holder.bind(folder, onRemoveClicked)
    }

    class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val folderPathTextView: TextView = itemView.findViewById(R.id.tvFolderPath)
        private val removeButton: ImageButton = itemView.findViewById(R.id.btnRemoveFolder)

        fun bind(folder: TrackedFolder, onRemoveClicked: (TrackedFolder) -> Unit) {
            folderPathTextView.text = folder.folderName
            removeButton.setOnClickListener { onRemoveClicked(folder) }
        }
    }

    class FolderDiffCallback : DiffUtil.ItemCallback<TrackedFolder>() {
        override fun areItemsTheSame(oldItem: TrackedFolder, newItem: TrackedFolder): Boolean {
            return oldItem.uri == newItem.uri
        }

        override fun areContentsTheSame(oldItem: TrackedFolder, newItem: TrackedFolder): Boolean {
            return oldItem == newItem
        }
    }
}
