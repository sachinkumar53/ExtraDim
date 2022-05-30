package com.sachin.app.extradim

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import de.dlyt.yanndroid.oneui.dialog.AlertDialog
import de.dlyt.yanndroid.oneui.layout.SwitchBarLayout
import de.dlyt.yanndroid.oneui.preference.PreferenceFragmentCompat
import de.dlyt.yanndroid.oneui.preference.PreferenceManager
import de.dlyt.yanndroid.oneui.preference.SeekBarPreference


class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val enabled = prefs.getBoolean("enabled", false)

        prefs.registerOnSharedPreferenceChangeListener(this)
        findViewById<SwitchBarLayout>(R.id.switch_bar_layout).apply {
            isChecked = enabled
            addOnSwitchChangeListener { _, isChecked ->
                val enabled = prefs?.getBoolean("enabled", false) ?: false
                if (enabled != isChecked)
                    prefs.edit().putBoolean("enabled", isChecked).apply()

                val action = if (isChecked) ACTION_SHOW_OVERLAY else ACTION_HIDE_OVERLAY
                sendBroadcast(Intent(action))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkService()
    }

    private fun checkService() {
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        if (!am.isEnabled) {
            AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("Accessibility service is not enabled. Turn on Extra Dim in accessibility settings first")
                .setPositiveButton("Open settings") { _, _ ->
                    try {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.setNegativeButton("Exit") { _, _ ->
                    finishAndRemoveTask()
                }.setCancelable(false)
                .show()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(bundle: Bundle?, key: String?) {
            setPreferencesFromResource(R.xml.preference_main, key)
            findPreference<SeekBarPreference>("intensity")?.setOnPreferenceChangeListener { preference, newValue ->
                val alpha = (newValue as Int) / 100f
                val intent = Intent(ACTION_UPDATE_OVERLAY)
                intent.putExtra("intensity", alpha)
                requireContext().sendBroadcast(intent)
                true
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            view.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.item_background_color
                )
            )
        }

    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        if (key == "enabled") {
            val enabled = prefs?.getBoolean("enabled", false) ?: false
            findViewById<SwitchBarLayout>(R.id.switch_bar_layout).apply {
                if (isChecked != enabled) {
                    isChecked = enabled
                }
            }
        }
    }

    override fun onDestroy() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }
}