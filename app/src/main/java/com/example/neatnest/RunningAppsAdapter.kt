package com.example.neatnest

import android.app.ActivityManager
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

// adapter for running apps in performance tab
class RunningAppsAdapter(
    private val onLongClick: (ActivityManager.RunningAppProcessInfo, View) -> Unit
) : ListAdapter<ActivityManager.RunningAppProcessInfo, RunningAppsAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ActivityManager.RunningAppProcessInfo>() {
            override fun areItemsTheSame(a: ActivityManager.RunningAppProcessInfo, b: ActivityManager.RunningAppProcessInfo) = a.pid == b.pid
            override fun areContentsTheSame(a: ActivityManager.RunningAppProcessInfo, b: ActivityManager.RunningAppProcessInfo) = a.processName == b.processName
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvAppName)
        val tvImportance: TextView = view.findViewById(R.id.tvAppImportance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_running_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = getItem(position)
        // show last part of process name for readability
        val displayName = app.processName.substringAfterLast(".")
        holder.tvName.text = displayName

        val (label, color) = when (app.importance) {
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND -> "Foreground" to "#4CAF50"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE -> "Visible" to "#2196F3"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE -> "Service" to "#FF9800"
            else -> "Background" to "#9E9E9E"
        }
        holder.tvImportance.text = label
        holder.tvImportance.setTextColor(Color.WHITE)
        holder.tvImportance.setBackgroundColor(Color.parseColor(color))

        holder.itemView.setOnLongClickListener {
            onLongClick(app, it)
            true
        }
    }
}
