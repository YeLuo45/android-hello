package com.hello.android.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.hello.android.MainActivity
import com.hello.android.R
import com.hello.android.data.datastore.CounterDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CounterWidgetReceiver : AppWidgetProvider() {

    companion object {
        const val ACTION_INCREMENT = "com.hello.android.ACTION_INCREMENT"
        const val EXTRA_WIDGET_ID = "widget_id"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val dataStore = CounterDataStore(context)
        CoroutineScope(Dispatchers.IO).launch {
            for (appWidgetId in appWidgetIds) {
                val counter = dataStore.counterFlow.first()
                updateAppWidget(context, appWidgetManager, appWidgetId, counter)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_INCREMENT) {
            val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val dataStore = CounterDataStore(context)
                CoroutineScope(Dispatchers.IO).launch {
                    val newValue = dataStore.incrementCounter()
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    updateAppWidget(context, appWidgetManager, widgetId, newValue)
                }
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        counterValue: Int
    ) {
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
