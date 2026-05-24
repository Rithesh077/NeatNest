package com.example.neatnest.ui.analyser

import android.app.ActivityManager
import android.graphics.Color
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.neatnest.R
import com.example.neatnest.RunningAppsAdapter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

// performance tab: line chart (RAM/CPU history) + running apps list
class PerformanceFragment : Fragment() {

    private val viewModel: DeviceAnalyserViewModel by activityViewModel()
    private lateinit var adapter: RunningAppsAdapter
    private var selectedApp: ActivityManager.RunningAppProcessInfo? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_performance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lineChart = view.findViewById<LineChart>(R.id.lineChart)
        val btnTimeRange = view.findViewById<Button>(R.id.btnTimeRange)
        val rvApps = view.findViewById<RecyclerView>(R.id.rvRunningApps)

        setupLineChart(lineChart)

        // running apps adapter with context menu
        adapter = RunningAppsAdapter { app, itemView ->
            selectedApp = app
            registerForContextMenu(itemView)
            requireActivity().openContextMenu(itemView)
        }
        rvApps.layoutManager = LinearLayoutManager(requireContext())
        rvApps.adapter = adapter

        // popup menu for time range
        btnTimeRange.setOnClickListener {
            val popup = PopupMenu(requireContext(), it)
            popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                val (rangeMs, label) = when (item.itemId) {
                    R.id.range_1h -> 60 * 60 * 1000L to "1h"
                    R.id.range_6h -> 6 * 60 * 60 * 1000L to "6h"
                    R.id.range_24h -> 24 * 60 * 60 * 1000L to "24h"
                    R.id.range_7d -> 7 * 24 * 60 * 60 * 1000L to "7d"
                    else -> 60 * 60 * 1000L to "1h"
                }
                btnTimeRange.text = label
                viewModel.setChartRange(rangeMs)
                true
            }
            popup.show()
        }

        // observe history for chart
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.history.collect { snapshots ->
                    if (snapshots.isEmpty()) return@collect

                    val ramEntries = snapshots.mapIndexed { i, s ->
                        Entry(i.toFloat(), (s.ramTotalMb - s.ramAvailableMb).toFloat())
                    }
                    val cpuEntries = snapshots.mapIndexed { i, s ->
                        Entry(i.toFloat(), s.cpuUsagePercent)
                    }

                    val ramSet = LineDataSet(ramEntries, "RAM (MB)").apply {
                        color = Color.parseColor("#7B1FA2")
                        lineWidth = 2f
                        setDrawCircles(false)
                        setDrawValues(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                    }
                    val cpuSet = LineDataSet(cpuEntries, "CPU (%)").apply {
                        color = Color.parseColor("#CE93D8")
                        lineWidth = 2f
                        setDrawCircles(false)
                        setDrawValues(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                    }

                    lineChart.data = LineData(ramSet, cpuSet)
                    lineChart.invalidate()
                }
            }
        }

        // observe running apps
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.runningApps.collect { apps ->
                    adapter.submitList(apps)
                }
            }
        }
    }

    // context menu for running apps
    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        requireActivity().menuInflater.inflate(R.menu.context_menu, menu)
        menu.setHeaderTitle(selectedApp?.processName ?: "App")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val app = selectedApp ?: return false
        return when (item.itemId) {
            R.id.action_app_info -> {
                Toast.makeText(requireContext(), "PID: ${app.pid}\nImportance: ${app.importance}", Toast.LENGTH_LONG).show()
                true
            }
            R.id.action_kill_app -> {
                Toast.makeText(requireContext(), "Force stop requires system permissions", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun setupLineChart(chart: LineChart) {
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            legend.textColor = Color.parseColor("#6A1B9A")
            axisLeft.textColor = Color.parseColor("#6A1B9A")
            axisRight.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.parseColor("#6A1B9A")
            xAxis.setDrawGridLines(false)
            animateX(500)
        }
    }
}
