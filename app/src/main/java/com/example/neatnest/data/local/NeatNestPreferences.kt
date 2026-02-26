package com.example.neatnest.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

// centralized preferences access
class NeatNestPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("NeatNestPrefs", Context.MODE_PRIVATE)

    var rootUri: String?
        get() = prefs.getString("root_uri", null)
        set(value) = prefs.edit { putString("root_uri", value) }

    var onboardingCompleted: Boolean
        get() = prefs.getBoolean("onboarding_completed", false)
        set(value) = prefs.edit { putBoolean("onboarding_completed", value) }

    var moveFilesEnabled: Boolean
        get() = prefs.getBoolean("move_files_enabled", false)
        set(value) = prefs.edit { putBoolean("move_files_enabled", value) }

    var completeScanMode: Boolean
        get() = prefs.getBoolean("complete_scan_mode", false)
        set(value) = prefs.edit { putBoolean("complete_scan_mode", value) }

    fun clearAll() = prefs.edit { clear() }
}
