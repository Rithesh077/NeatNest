package com.example.neatnest.ui.analyser

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.neatnest.R
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

// storage tab: pie chart showing used vs available + details
class StorageFragment : Fragment() {

    private val viewModel: DeviceAnalyserViewModel by activityViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_storage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pieChart = view.findViewById<PieChart>(R.id.pieChart)
        val tvDetails = view.findViewById<TextView>(R.id.tvStorageDetails)

        setupPieChart(pieChart)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentStats.collect { stats ->
                    if (stats == null) return@collect

                    val usedMb = stats.storageTotalMb - stats.storageAvailableMb
                    val usedGb = usedMb / 1024f
                    val availGb = stats.storageAvailableMb / 1024f
                    val totalGb = stats.storageTotalMb / 1024f

                    // update pie chart
                    val entries = listOf(
                        PieEntry(usedMb.toFloat(), "Used"),
                        PieEntry(stats.storageAvailableMb.toFloat(), "Available")
                    )
                    val dataSet = PieDataSet(entries, "").apply {
                        colors = listOf(
                            Color.parseColor("#7B1FA2"), // purple (used)
                            Color.parseColor("#CE93D8")  // light purple (available)
                        )
                        valueTextColor = Color.WHITE
                        valueTextSize = 13f
                        sliceSpace = 2f
                    }
                    pieChart.data = PieData(dataSet)
                    pieChart.centerText = "%.1f GB\nTotal".format(totalGb)
                    pieChart.invalidate()

                    // details text
                    tvDetails.text = buildString {
                        append("Total:  %.1f GB\n".format(totalGb))
                        append("Used:   %.1f GB (%.0f%%)\n".format(usedGb, usedMb * 100f / stats.storageTotalMb))
                        append("Free:   %.1f GB (%.0f%%)".format(availGb, stats.storageAvailableMb * 100f / stats.storageTotalMb))
                    }
                }
            }
        }
    }

    private fun setupPieChart(chart: PieChart) {
        chart.apply {
            setUsePercentValues(false)
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 55f
            transparentCircleRadius = 60f
            setCenterTextColor(Color.parseColor("#6A1B9A"))
            setCenterTextSize(14f)
            legend.isEnabled = true
            legend.textColor = Color.parseColor("#6A1B9A")
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
            animateY(600)
        }
    }
}
