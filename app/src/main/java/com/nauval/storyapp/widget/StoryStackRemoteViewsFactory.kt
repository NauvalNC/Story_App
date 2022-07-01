package com.nauval.storyapp.widget

import android.content.Context
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.nauval.storyapp.R
import com.nauval.storyapp.helper.StoryApiConfig
import com.nauval.storyapp.helper.StoryApiSession
import com.nauval.storyapp.helper.StoryItemResponse
import com.nauval.storyapp.helper.Utils
import java.io.IOException

internal class StoryStackRemoteViewsFactory(private val ctx: Context) : RemoteViewsService.RemoteViewsFactory {

    private var stories: List<StoryItemResponse> = ArrayList()

    override fun onDataSetChanged()
    {
        try {
            stories = StoryApiConfig.getStoryApiService()
                .getStoriesSync("Bearer ${StoryApiSession(ctx).getToken()}").execute().body()!!.stories
        } catch (e: IOException) { e.printStackTrace() }
    }

    override fun getViewAt(position: Int): RemoteViews {
        return RemoteViews(ctx.packageName, R.layout.story_widget_item).apply {
            setTextViewText(R.id.post_username, stories[position].name)
            setImageViewBitmap(R.id.post_image, Utils.convertURLToBitmap(stories[position].photoUrl))
        }
    }

    override fun onCreate() { }
    override fun getCount(): Int = stories.size
    override fun onDestroy() { }
    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = 0
    override fun hasStableIds(): Boolean = false
}