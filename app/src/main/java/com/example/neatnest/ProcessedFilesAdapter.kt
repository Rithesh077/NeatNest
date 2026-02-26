package com.example.neatnest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ProcessedFilesAdapter(private val onInfoClicked: (ProcessedFile) -> Unit) :
    ListAdapter<ProcessedFile, ProcessedFilesAdapter.FileViewHolder>(FileDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_processed_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = getItem(position)
        holder.bind(file, onInfoClicked)
    }

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFileName: TextView = itemView.findViewById(R.id.tvFileName)
        private val tvFileExtension: TextView = itemView.findViewById(R.id.tvFileExtension)
        private val btnFileInfo: ImageButton = itemView.findViewById(R.id.btnFileInfo)

        fun bind(file: ProcessedFile, onInfoClicked: (ProcessedFile) -> Unit) {
            tvFileName.text = file.fileName
            tvFileExtension.text = file.extension.uppercase()
            btnFileInfo.setOnClickListener { onInfoClicked(file) }
        }
    }

    class FileDiffCallback : DiffUtil.ItemCallback<ProcessedFile>() {
        override fun areItemsTheSame(oldItem: ProcessedFile, newItem: ProcessedFile): Boolean {
            return oldItem.originalUri == newItem.originalUri
        }

        override fun areContentsTheSame(oldItem: ProcessedFile, newItem: ProcessedFile): Boolean {
            return oldItem == newItem
        }
    }
}

