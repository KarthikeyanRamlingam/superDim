package com.example.superdim

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.math.max
import kotlin.math.min

class SliderPopupActivity : Activity() {

    private var currentLevel = 60
    private lateinit var fillView: View
    private val maxLevel = 80

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("SuperDimPrefs", Context.MODE_PRIVATE)
        currentLevel = prefs.getInt("dimLevel", 60)

        // Build the programmatic UI mimicking the widget
        val rootLayout = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // Tap outside to close
            setOnClickListener { finish() }
        }

        val sliderContainer = FrameLayout(this).apply {
            val widthPx = (80 * resources.displayMetrics.density).toInt()
            val heightPx = (180 * resources.displayMetrics.density).toInt()
            layoutParams = FrameLayout.LayoutParams(widthPx, heightPx).apply {
                gravity = Gravity.CENTER
            }
            background = GradientDrawable().apply {
                cornerRadius = 24f * resources.displayMetrics.density
                setColor(Color.parseColor("#4D2E2B")) // dark bg
            }
        }

        fillView = View(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0 // Height set dynamically
            ).apply {
                gravity = Gravity.BOTTOM
            }
            background = GradientDrawable().apply {
                cornerRadius = 24f * resources.displayMetrics.density
                setColor(Color.parseColor("#F4E6D9")) // light bg
            }
        }

        val sunIcon = ImageView(this).apply {
            val size = (24 * resources.displayMetrics.density).toInt()
            layoutParams = FrameLayout.LayoutParams(size, size).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = (16 * resources.displayMetrics.density).toInt()
            }
            setImageResource(R.drawable.ic_sun)
        }

        sliderContainer.addView(fillView)
        sliderContainer.addView(sunIcon)
        rootLayout.addView(sliderContainer)

        setContentView(rootLayout)

        updateFillHeight()

        sliderContainer.setOnTouchListener { v, event ->
            val y = event.y
            val height = v.height
            // Calculate level based on touch position (bottom is 0, top is maxLevel)
            var newLevel = maxLevel - ((y / height) * maxLevel).toInt()
            newLevel = max(0, min(maxLevel, newLevel))

            if (newLevel != currentLevel) {
                currentLevel = newLevel
                updateFillHeight()
                applyLevel()
            }

            if (event.action == MotionEvent.ACTION_UP) {
                // Save and close
                prefs.edit().putInt("dimLevel", currentLevel).apply()
                sendBroadcast(Intent(this, DimWidgetProvider::class.java).setAction("UPDATE_WIDGET"))
                // Don't finish immediately, maybe let user see? Actually, standard is wait for tap outside.
            }
            true
        }
    }

    private fun updateFillHeight() {
        fillView.post {
            val parent = fillView.parent as View
            val params = fillView.layoutParams as FrameLayout.LayoutParams
            val targetHeight = (parent.height * currentLevel) / maxLevel
            if (params.height != targetHeight) {
                params.height = targetHeight
                fillView.layoutParams = params
            }
        }
    }

    private fun applyLevel() {
        if (Settings.canDrawOverlays(this)) {
            startService(
                Intent(this, DimService::class.java)
                    .putExtra("alpha", currentLevel)
            )
        }
    }
}
