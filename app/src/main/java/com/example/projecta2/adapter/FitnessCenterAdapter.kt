package com.example.projecta2.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projecta2.Entity.UserInfo
import com.example.projecta2.R
import com.example.projecta2.View.CenterDetailActivity
import com.example.projecta2.databinding.FitnessCenterItemBinding
import com.example.projecta2.model.FitnessCenter
import com.example.projecta2.util.getUserObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FitnessCenterAdapter(public var fitnessCenterList: List<FitnessCenter>, private val userInfo: UserInfo?) :
    RecyclerView.Adapter<FitnessCenterAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FitnessCenterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fitnessCenter = fitnessCenterList[position]
        with(holder) {
            // 이미지 로드 및 표시
            fitnessCenter.imagePath?.let {
                if (it.isNotEmpty()) {
                    val imageUrl = "http://10.100.103.27:8111/img/$it"
                    Glide.with(binding.ivFitnessCenterImage.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.chair_white_bg) // 로딩 중에 표시할 이미지
                        .error(R.drawable.chair_light_orange_bg) // 에러 발생 시 표시할 이미지
                        .into(binding.ivFitnessCenterImage)
                } else {
                    // 이미지가 없을 경우에 대한 처리
                    binding.ivFitnessCenterImage.setImageResource(R.drawable.favorite_img_7)
                }
            }

            // 나머지 데이터 표시
            binding.tvFitnessCenterName.text = fitnessCenter.name
            binding.tvFitnessCenterDailyPassPrice.text = "${fitnessCenter.dailyPassPrice}원"
            binding.tvFitnessCenterdistance.text = "${fitnessCenter.distance?.toInt()} m"
            binding.tvFitnessCenterAddress.text = fitnessCenter.address

            // 클릭 리스너 설정
            itemView.setOnClickListener {
                // 클릭 시 유저 정보를 가져옴
                CoroutineScope(Dispatchers.Main).launch {
                    val userInfo: UserInfo? = getUserObject(it.context).getUserInfo()
                    val intent = Intent(it.context, CenterDetailActivity::class.java).apply {
                        putExtra("centerId", fitnessCenter.id) // 센터 아이디
                        putExtra("centerName", fitnessCenter.name) // 센터 이름
                        putExtra("centerPrice", fitnessCenter.dailyPassPrice) // 센터 가격
                        putExtra("centerLocation", fitnessCenter.address) // 센터 위치
                        putExtra("centerImageUrl", fitnessCenter.imagePath?.let { imagePath ->
                            "http://10.100.103.27:8111/img/$imagePath"
                        }) // 센터 이미지 URL
                        putExtra("userInfo", userInfo) // 유저 정보 전달
                    }
                    it.context.startActivity(intent)
                }
            }
        }
    }

    fun updateList(newList: List<FitnessCenter>) {
        fitnessCenterList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = fitnessCenterList.size

    class ViewHolder(val binding: FitnessCenterItemBinding) : RecyclerView.ViewHolder(binding.root)
}
