package com.nauval.storyapp.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nauval.storyapp.helper.StoryItemResponse

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryItemResponse>)

    @Query("SELECT * FROM story")
    fun getAllStory(): PagingSource<Int, StoryItemResponse>

    @Query("DELETE FROM story")
    suspend fun deleteAllStories()
}