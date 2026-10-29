
package com.example.superdim

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class DimService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlay: View? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        val alpha = (intent?.getIntExtra("alpha", 60) ?: 60)
            .coerceIn(0, 80)

        if (overlay == null) {
            overlay = View(this).apply {
                isClickable = false
                isFocusable = false
                systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR,
                PixelFormat.TRANSLUCENT
            )

            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

            params.gravity = Gravity.TOP or Gravity.START
            windowManager.addView(overlay, params)
        }

        // Slider value 0–80 maps to overlay opacity 0–204.
        overlay?.setBackgroundColor(
            Color.argb(alpha * 255 / 100, 0, 0, 0)
        )

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        overlay?.let {
            if (it.isAttachedToWindow) {
                windowManager.removeView(it)
            }
        }
        overlay = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
