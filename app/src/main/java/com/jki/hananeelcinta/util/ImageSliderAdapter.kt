package com.jki.hananeelcinta.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jki.hananeelcinta.R

class ImageSliderAdapter(private val imageUrls: List<String>) :
    RecyclerView.Adapter<ImageSliderAdapter.ImageSlideViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageSlideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_slide, parent, false)
        return ImageSlideViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageSlideViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        holder.bind(imageUrl)
    }

    override fun getItemCount(): Int = imageUrls.size

    inner class ImageSlideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(imageUrl: String) {
            Glide.with(itemView)
                .load(imageUrl)
                .centerCrop()
                .into(itemView.findViewById(R.id.imageView))
        }
    }
}