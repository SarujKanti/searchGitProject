package com.skd.githubsearch.adapter

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.skd.githubsearch.dataModel.GHRepo
import com.skd.githubsearch.databinding.ItemRepoBinding

class RepoAdapter(private val onClick: (GHRepo) -> Unit, private val onDataLoaded: () -> Unit) :
    RecyclerView.Adapter<RepoAdapter.RepoViewHolder>() {

    private var list = listOf<GHRepo>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<GHRepo>) {
        list = newList
        notifyDataSetChanged()
        onDataLoaded()
    }

    inner class RepoViewHolder(private val binding: ItemRepoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: GHRepo) {
            binding.repoName.text = "Name: ${item.name}"
            binding.repoId.text = "ID: ${item.id}"

            // Detect current night mode
            val isDarkMode = (binding.root.context.resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

            val textColor = if (isDarkMode) {
                ContextCompat.getColor(binding.root.context, android.R.color.white)
            } else {
                ContextCompat.getColor(binding.root.context, android.R.color.black)
            }

            // Apply text color
            binding.repoName.setTextColor(textColor)
            binding.repoId.setTextColor(textColor)

            // Click listener
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RepoViewHolder(
            ItemRepoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun getItemCount() = list.size
    override fun onBindViewHolder(holder: RepoViewHolder, position: Int) =
        holder.bind(list[position])
}