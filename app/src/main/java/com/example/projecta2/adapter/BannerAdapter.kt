package com.example.projecta2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.projecta2.R
import com.example.projecta2.databinding.FitnessCenterItemBinding
import com.example.projecta2.model.FitnessCenter

class BannerAdapter(private val images: List<Int>) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.banner_item, parent, false)
        return BannerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val imageResId = images[position]
        holder.imageView.setImageResource(imageResId) // 이미지 리소스 ID를 사용하여 이미지 설정
    }

    override fun getItemCount(): Int = images.size

    class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}