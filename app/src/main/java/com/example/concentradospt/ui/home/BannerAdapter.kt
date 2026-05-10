package com.example.concentradospt.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.concentradospt.R
import com.example.concentradospt.databinding.ItemBannerSlideBinding

class BannerAdapter(private var urls: List<String>) :
    RecyclerView.Adapter<BannerAdapter.SlideViewHolder>() {

    inner class SlideViewHolder(private val binding: ItemBannerSlideBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(url: String) {
            Glide.with(binding.root)
                .load(url)
                .placeholder(R.color.surface_container)
                .centerCrop()
                .into(binding.bannerSlideImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
        val binding = ItemBannerSlideBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SlideViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SlideViewHolder, position: Int) =
        holder.bind(urls[position])

    override fun getItemCount() = urls.size

    fun updateUrls(newUrls: List<String>) {
        urls = newUrls
        notifyDataSetChanged()
    }
}
