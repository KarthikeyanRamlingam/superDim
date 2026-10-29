
package com.example.superdim

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.LinearLayout
import android.view.Gravity
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val requestCode = 100
    private var dimLevel = 60

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("SuperDimPrefs", MODE_PRIVATE)
        dimLevel = prefs.getInt("dimLevel", 60)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.rgb(18, 18, 18))
        }

        val title = TextView(this).apply {
            text = "SuperDim"
            textSize = 30f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val description = TextView(this).apply {
            text = "Reduce your screen brightness further"
            textSize = 16f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        val levelText = TextView(this).apply {
            text = "Dim intensity: $dimLevel%"
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val slider = SeekBar(this).apply {
            max = 80
            progress = dimLevel
        }

        val enableButton = Button(this).apply {
            text = "Enable SuperDim"
        }

        val disableButton = Button(this).apply {
            text = "Disable SuperDim"
        }

        layout.addView(title)
        layout.addView(description)
        layout.addView(levelText)
        layout.addView(slider)
        layout.addView(enableButton)
        layout.addView(disableButton)

        setContentView(layout)

        slider.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    dimLevel = progress
                    levelText.text = "Dim intensity: $dimLevel%"
                    val prefs = getSharedPreferences("SuperDimPrefs", MODE_PRIVATE)
                    prefs.edit().putInt("dimLevel", dimLevel).apply()
                    sendBroadcast(Intent(this@MainActivity, DimWidgetProvider::class.java).setAction("UPDATE_WIDGET"))
                    if (Settings.canDrawOverlays(this@MainActivity)) {
                        startService(
                            Intent(this@MainActivity, DimService::class.java)
                                .putExtra("alpha", dimLevel)
                        )
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )

        enableButton.setOnClickListener {
            if (Settings.canDrawOverlays(this)) {
                startService(
                    Intent(this, DimService::class.java)
                        .putExtra("alpha", dimLevel)
                )
            } else {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                @Suppress("DEPRECATION")
                startActivityForResult(intent, requestCode)
            }
        }

        disableButton.setOnClickListener {
            stopService(Intent(this, DimService::class.java))
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == this.requestCode &&
            Settings.canDrawOverlays(this)
        ) {
            startService(
                Intent(this, DimService::class.java)
                    .putExtra("alpha", dimLevel)
            )
        }
    }
}
