package com.sachin.app.extradim

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import de.dlyt.yanndroid.oneui.preference.PreferenceManager

private const val TAG = "ExtraDimAccessibilitySe"
const val ACTION_SHOW_OVERLAY = "action.SHOW_OVERLAY"
const val ACTION_HIDE_OVERLAY = "action.HIDE_OVERLAY"
const val ACTION_UPDATE_OVERLAY = "action.UPDATE_OVERLAY"

class ExtraDimAccessibilityService : AccessibilityService() {
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            if (intent?.action == ACTION_SHOW_OVERLAY) {
                if (!overlay.isAttachedToWindow) {
                    showOverlay()
                }
            } else if (intent?.action == ACTION_HIDE_OVERLAY) {
                if (overlay.isAttachedToWindow)
                    hideOverlay()
            } else if (intent?.action == ACTION_UPDATE_OVERLAY) {
                val alpha = intent.extras?.getFloat("intensity")
                if (alpha != null) {
                    updateOverlay(alpha)
                }
            }
        }
    }

    private val params = WindowManager.LayoutParams().apply {
        type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        format = PixelFormat.TRANSLUCENT
        width = 1080
        height = 2400
        flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
    }
    private val overlay by lazy {
        FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            fitsSystemWindows = true
            val prefs =
                PreferenceManager.getDefaultSharedPreferences(this@ExtraDimAccessibilityService)
            val alpha = prefs.getInt("intensity", 10) / 100f
            setBackgroundColor(Color.argb(alpha, 0f, 0f, 0f))
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val enabled = prefs.getBoolean("enabled", false)
        if (enabled) {
            showOverlay()
        }

        registerReceiver(receiver, IntentFilter().apply {
            addAction(ACTION_SHOW_OVERLAY)
            addAction(ACTION_HIDE_OVERLAY)
            addAction(ACTION_UPDATE_OVERLAY)
        })

    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }


    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

    }

    private fun showOverlay() {
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        wm.addView(overlay, params)
    }

    private fun hideOverlay() {
        if (overlay.isAttachedToWindow) {
            val wm = getSystemService(WINDOW_SERVICE) as WindowManager
            wm.removeViewImmediate(overlay)
        }
    }

    private fun updateOverlay(alpha: Float) {
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val color = Color.argb(alpha, 0f, 0f, 0f)
        overlay.setBackgroundColor(color)
        if (overlay.isAttachedToWindow)
            wm.updateViewLayout(overlay, params)
    }

    override fun onInterrupt() {
    }
}