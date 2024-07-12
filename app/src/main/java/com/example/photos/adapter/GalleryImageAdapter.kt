package com.example.photos.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.photos.databinding.ItemGalleryImageBinding

class GalleryImageAdapter(private val itemList: List<Image>) : RecyclerView.Adapter<GalleryImageAdapter.ViewHolder>() {

    private var context: Context? = null
    var listener: GalleryImageClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : GalleryImageAdapter.ViewHolder {
        context = parent.context
        val binding = ItemGalleryImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: GalleryImageAdapter.ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    inner class ViewHolder(private val binding: ItemGalleryImageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(image: Image) {
            // Load image
            Glide.with(context!!)
                .load(image.imagePath)
                .centerCrop()
                .into(binding.ivGalleryImage)

            // Adding click or tap handler for our image layout
            binding.container.setOnClickListener {
                listener?.onClick(adapterPosition)
            }
        }
    }
}
