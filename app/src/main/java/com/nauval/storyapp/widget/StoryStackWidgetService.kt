package com.nauval.storyapp.widget

import android.content.Intent
import android.widget.RemoteViewsService

class StoryStackWidgetService: RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory =
        StoryStackRemoteViewsFactory(this.applicationContext)
}