package com.sachin.app.extradim

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import de.dlyt.yanndroid.oneui.preference.PreferenceManager

class ExtraDimTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val isEnabled = prefs.getBoolean("enabled", false)
        qsTile?.apply {
            state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()

        if (!checkService()) return

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val nextState = !prefs.getBoolean("enabled", false)
        val action = if (nextState) ACTION_SHOW_OVERLAY else ACTION_HIDE_OVERLAY
        sendBroadcast(Intent(action))
        prefs.edit().putBoolean("enabled", nextState).apply()
        qsTile?.apply {
            state = if (nextState) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTile()
        }
    }

    private fun checkService(): Boolean {
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val isEnabled = am.isEnabled
        if (!isEnabled) {
            Toast.makeText(
                this,
                "Turn on Extra dim in accessibility settings first",
                Toast.LENGTH_SHORT
            ).show()
        }
        return isEnabled
    }

    override fun onStopListening() {
        super.onStopListening()

    }
}