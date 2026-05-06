package com.hello.android.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews
import com.hello.android.MainActivity
import com.hello.android.R

class CounterWidgetReceiver : AppWidgetProvider() {

    companion object {
        const val ACTION_INCREMENT = "com.hello.android.ACTION_INCREMENT"
        const val EXTRA_WIDGET_ID = "widget_id"
        private const val PREFS_NAME = "counter_widget_prefs"
        private const val KEY_COUNTER = "counter_value"

        fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }

        fun getCounterValue(context: Context): Int {
            return getSharedPreferences(context).getInt(KEY_COUNTER, 0)
        }

        fun incrementCounter(context: Context): Int {
            val prefs = getSharedPreferences(context)
            val newValue = prefs.getInt(KEY_COUNTER, 0) + 1
            prefs.edit().putInt(KEY_COUNTER, newValue).apply()
            return newValue
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_INCREMENT) {
            val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val newValue = incrementCounter(context)
                val appWidgetManager = AppWidgetManager.getInstance(context)
                updateAppWidget(context, appWidgetManager, widgetId)

                // Also update MainViewModel via SharedPreferences for next app launch
                val mainPrefs = context.getSharedPreferences("counter_prefs", Context.MODE_PRIVATE)
                mainPrefs.edit().putInt("counter_value", newValue).apply()
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val counterValue = getCounterValue(context)

        // Intent to open app
        val openAppIntent = Intent(context, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to increment counter
        val incrementIntent = Intent(context, CounterWidgetReceiver::class.java).apply {
            action = ACTION_INCREMENT
            putExtra(EXTRA_WIDGET_ID, appWidgetId)
        }
        val incrementPendingIntent = PendingIntent.getBroadcast(
            context, appWidgetId, incrementIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val views = RemoteViews(context.packageName, R.layout.widget_counter).apply {
            setTextViewText(R.id.widget_counter_text, counterValue.toString())
            setOnClickPendingIntent(R.id.widget_counter_text, openAppPendingIntent)
            setOnClickPendingIntent(R.id.widget_increment_button, incrementPendingIntent)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
