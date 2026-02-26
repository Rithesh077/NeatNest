package com.example.neatnest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentActivityAdapter(private val onItemClicked: (RecentActivityItem) -> Unit) :
    ListAdapter<RecentActivityItem, RecentActivityAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onItemClicked)
    }

    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivActivityIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvActivityTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvActivityDescription)
        private val tvTime: TextView = itemView.findViewById(R.id.tvActivityTime)

        fun bind(item: RecentActivityItem, onItemClicked: (RecentActivityItem) -> Unit) {
            tvTitle.text = item.title
            tvDescription.text = item.description
            tvTime.text = getRelativeTime(item.timestamp)

            ivIcon.setImageResource(
                when (item.type) {
                    RecentActivityItem.ActivityType.FILE_SCANNED -> android.R.drawable.ic_menu_search
                    RecentActivityItem.ActivityType.FILE_ORGANIZED -> android.R.drawable.ic_menu_sort_by_size
                    RecentActivityItem.ActivityType.NOTIFICATION_CAPTURED -> android.R.drawable.ic_dialog_email
                    RecentActivityItem.ActivityType.SYNC_COMPLETED -> android.R.drawable.ic_popup_sync
                    RecentActivityItem.ActivityType.RESET_PERFORMED -> android.R.drawable.ic_menu_revert
                    RecentActivityItem.ActivityType.APP_LAUNCHED -> android.R.drawable.ic_menu_recent_history
                }
            )

            itemView.setOnClickListener { onItemClicked(item) }
        }

        private fun getRelativeTime(timestamp: Long): String {
            val diff = System.currentTimeMillis() - timestamp
            return when {
                diff < 60_000 -> "Just now"
                diff < 3_600_000 -> "${diff / 60_000}m ago"
                diff < 86_400_000 -> "${diff / 3_600_000}h ago"
                else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }

    class ActivityDiffCallback : DiffUtil.ItemCallback<RecentActivityItem>() {
        override fun areItemsTheSame(oldItem: RecentActivityItem, newItem: RecentActivityItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RecentActivityItem, newItem: RecentActivityItem): Boolean {
            return oldItem == newItem
        }
    }
}

