package com.jki.myhananeelcinta.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jki.myhananeelcinta.R
import com.jki.myhananeelcinta.model.Announcement

class ImageSliderAdapter(private val imageUrls: List<Announcement>) :
    RecyclerView.Adapter<ImageSliderAdapter.ImageSlideViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(announcement: Announcement)
    }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageSlideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_slide, parent, false)
        return ImageSlideViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageSlideViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        imageUrl.imageUrl?.let { holder.bind(it) }

        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClick(imageUrl)
        }
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