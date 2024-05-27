package com.example.projecta2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projecta2.R
import com.example.projecta2.databinding.HomeFitnessListItemBinding
import com.example.projecta2.model.FitnessCenter

class HomeAdapter(
    private val fitnessCenterList: List<FitnessCenter>,
    private val onItemClicked: (FitnessCenter) -> Unit // 클릭 리스너를 매개변수로 추가
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HomeFitnessListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fitnessCenter = fitnessCenterList[position]
        with(holder) {
            binding.apply {
                // 이미지 로드 및 표시
                Glide.with(homePageCenterImg.context)
                    .load(fitnessCenter.imagePath?.let { "http://10.100.103.27:8111/img/$it" })
                    .placeholder(R.drawable.chair_white_bg) // 로딩 중에 표시할 이미지
                    .error(R.drawable.chair_light_orange_bg) // 에러 발생 시 표시할 이미지
                    .into(homePageCenterImg)

                // 나머지 데이터 표시
                textViewItemName.text = fitnessCenter.name
                textViewItemPrice.text = "${fitnessCenter.dailyPassPrice}원"

                root.setOnClickListener {
                    onItemClicked(fitnessCenter) // 클릭된 항목의 정보를 전달
                }
            }
        }
    }

    override fun getItemCount(): Int = fitnessCenterList.size

    class ViewHolder(val binding: HomeFitnessListItemBinding) : RecyclerView.ViewHolder(binding.root)
}
