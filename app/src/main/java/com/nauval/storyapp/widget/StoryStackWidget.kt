package com.nauval.storyapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.net.toUri
import com.nauval.storyapp.R

class StoryStackWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) onUpdateWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onEnabled(context: Context) { }

    override fun onDisabled(context: Context) { }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != null) {
            if (intent.action == REFRESH_ACTION) {
                Toast.makeText(context, context.resources.getString(R.string.refreshing), Toast.LENGTH_LONG).show()
                val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(widgetId, R.id.stack_view)
            }
        }
    }

    companion object {
        private const val REFRESH_ACTION = "com.nauval.storyapp.REFRESH_ACTION"

        private fun onUpdateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val widgetServiceIntent = Intent(context, StoryStackWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = this.toUri(Intent.URI_INTENT_SCHEME).toUri()
            }

            val widgetRemoteViews = RemoteViews(context.packageName, R.layout.story_stack_widget).apply {
                setEmptyView(R.id.stack_view, R.id.no_data)
                setRemoteAdapter(R.id.stack_view, widgetServiceIntent)

                setOnClickPendingIntent(R.id.banner_txt, PendingIntent.getBroadcast(
                        context, appWidgetId,
                        Intent(context, StoryStackWidget::class.java).apply {
                            action = REFRESH_ACTION
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        },
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                        else 0
                    )
                )
            }

            appWidgetManager.updateAppWidget(appWidgetId, widgetRemoteViews)
        }
    }
}