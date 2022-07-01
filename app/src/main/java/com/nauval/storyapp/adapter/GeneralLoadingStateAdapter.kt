package com.nauval.storyapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nauval.storyapp.databinding.GeneralLoadingStateBinding

class GeneralLoadingStateAdapter(private val retryAction: () -> Unit) :
    LoadStateAdapter<GeneralLoadingStateAdapter.GeneralLoadingStateViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): GeneralLoadingStateViewHolder {
        val binding = GeneralLoadingStateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GeneralLoadingStateViewHolder(binding, retryAction)
    }

    override fun onBindViewHolder(holder: GeneralLoadingStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class GeneralLoadingStateViewHolder(
        private val binding: GeneralLoadingStateBinding,
        retryAction: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root)
    {
        init { binding.retryButton.setOnClickListener { retryAction.invoke() } }

        fun bind(loadState: LoadState) {
            binding.apply {
                if (loadState is LoadState.Error) errorMessage.text = loadState.error.localizedMessage
                binding.retryButton.isVisible = loadState is LoadState.Error
                binding.errorMessage.isVisible = loadState is LoadState.Error
                loadingBar.isVisible = loadState is LoadState.Loading
            }
        }
    }
}