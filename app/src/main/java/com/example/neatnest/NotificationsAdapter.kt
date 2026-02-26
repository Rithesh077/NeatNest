package com.example.neatnest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class NotificationsAdapter :
    ListAdapter<ProcessedNotification, NotificationsAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = getItem(position)
        holder.bind(notification)
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvNotifTitle)
        private val tvPackage: TextView = itemView.findViewById(R.id.tvNotifPackage)
        private val tvPriority: TextView = itemView.findViewById(R.id.tvNotifPriority)

        fun bind(notification: ProcessedNotification) {
            tvTitle.text = notification.title ?: itemView.context.getString(R.string.notif_no_title)
            tvPackage.text = notification.packageName
            tvPriority.text = notification.priority
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<ProcessedNotification>() {
        override fun areItemsTheSame(oldItem: ProcessedNotification, newItem: ProcessedNotification): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProcessedNotification, newItem: ProcessedNotification): Boolean {
            return oldItem == newItem
        }
    }
}

