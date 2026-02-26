package com.example.neatnest

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

// demonstrates fragment lifecycle callbacks
class LifecycleFragment : Fragment() {

    private val tag = "LifecycleFragment"
    private lateinit var tvLifecycleLog: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(tag, "onAttach")
        appendLog("onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "onCreateView")
        return inflater.inflate(R.layout.fragment_lifecycle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(tag, "onViewCreated")
        tvLifecycleLog = view.findViewById(R.id.tvLifecycleLog)
        appendLog("onViewCreated: fragment is ready")
    }

    override fun onStart() {
        super.onStart()
        Log.d(tag, "onStart")
        appendLog("onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "onResume")
        appendLog("onResume: visible and interactive")
    }

    override fun onPause() {
        super.onPause()
        Log.d(tag, "onPause")
        appendLog("onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(tag, "onStop")
        appendLog("onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(tag, "onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(tag, "onDetach")
    }

    private fun appendLog(event: String) {
        if (::tvLifecycleLog.isInitialized) {
            val existing = tvLifecycleLog.text.toString()
            tvLifecycleLog.text = if (existing.isEmpty()) event else "$existing\n$event"
        }
    }
}
