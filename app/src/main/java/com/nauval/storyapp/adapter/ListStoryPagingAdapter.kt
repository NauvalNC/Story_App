package com.nauval.storyapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nauval.storyapp.databinding.StoryItemBinding
import com.nauval.storyapp.helper.StoryItemResponse
import com.nauval.storyapp.helper.Utils
import com.nauval.storyapp.helper.Utils.formatDate

class ListStoryPagingAdapter: PagingDataAdapter<StoryItemResponse, ListStoryPagingAdapter.StoryItemPagingHolder>(DIFF_CALLBACK)
{
    private lateinit var onItemClickCallback: OnItemClickCallback

    class StoryItemPagingHolder(private val binding: StoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: StoryItemResponse, itemClickCallback: OnItemClickCallback, itemPos: Int) {
            binding.postUsername.text = story.name
            binding.postDate.text = story.createdAt.formatDate()
            Glide.with(itemView.context)
                .load(story.photoUrl)
                .thumbnail(0.5f)
                .override( 256, 256)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(Utils.getCircularProgressDrawable(itemView.context))
                .into(binding.postImage)

            itemView.setOnClickListener { itemClickCallback.onItemClicked(story, itemPos) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryItemPagingHolder {
        val binding = StoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryItemPagingHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryItemPagingHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it, onItemClickCallback, position)
        }
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(story: StoryItemResponse, position: Int)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryItemResponse>() {
            override fun areItemsTheSame(oldItem: StoryItemResponse, newItem: StoryItemResponse): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: StoryItemResponse, newItem: StoryItemResponse): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}