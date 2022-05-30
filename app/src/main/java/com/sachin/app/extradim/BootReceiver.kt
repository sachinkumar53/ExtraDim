package com.sachin.app.extradim

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.dlyt.yanndroid.oneui.preference.PreferenceManager

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            try {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context!!)
                val keepOn = prefs.getBoolean("keep_on", false)
                val isEnabled = prefs.getBoolean("enabled", false)
                if (keepOn && isEnabled) {
                    context.sendBroadcast(Intent(ACTION_SHOW_OVERLAY))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}