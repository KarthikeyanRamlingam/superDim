package com.example.superdim

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class DimWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs = context.getSharedPreferences("SuperDimPrefs", Context.MODE_PRIVATE)
        val level = prefs.getInt("dimLevel", 60)

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, level)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "UPDATE_WIDGET" || intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, DimWidgetProvider::class.java)
            )
            val prefs = context.getSharedPreferences("SuperDimPrefs", Context.MODE_PRIVATE)
            val level = prefs.getInt("dimLevel", 60)

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, level)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        level: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_super_dim)

        // Set the clip level (0 to 10000). Max dim is 80.
        // If level = 80, clip is 10000 (fully covering).
        val clipLevel = (level * 10000) / 80
        views.setInt(R.id.widget_fill, "setImageLevel", clipLevel)

        // Launch Transparent Popup on click
        val intent = Intent(context, SliderPopupActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
