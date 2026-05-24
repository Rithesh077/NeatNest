package com.example.neatnest.ui.analyser

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
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

// network tab: connection type, WiFi details
class NetworkFragment : Fragment() {

    private val viewModel: DeviceAnalyserViewModel by activityViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_network, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvType = view.findViewById<TextView>(R.id.tvConnectionType)
        val tvStatus = view.findViewById<TextView>(R.id.tvConnectionStatus)
        val cardWifi = view.findViewById<MaterialCardView>(R.id.cardWifiDetails)
        val tvSsid = view.findViewById<TextView>(R.id.tvWifiSsid)
        val tvIp = view.findViewById<TextView>(R.id.tvWifiIp)
        val tvSpeed = view.findViewById<TextView>(R.id.tvWifiSpeed)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.networkInfo.collect { info ->
                    if (info == null) return@collect

                    tvType.text = info.type
                    tvStatus.text = if (info.isConnected) "Connected" else "No connection"

                    if (info.type == "WiFi" && info.wifiSsid != null) {
                        cardWifi.visibility = View.VISIBLE
                        tvSsid.text = "SSID: ${info.wifiSsid}"
                        tvIp.text = "IP: ${info.ipAddress ?: "Unknown"}"
                        tvSpeed.text = "Link Speed: ${info.linkSpeed ?: "--"} Mbps"
                    } else {
                        cardWifi.visibility = View.GONE
                    }
                }
            }
        }
    }
}
