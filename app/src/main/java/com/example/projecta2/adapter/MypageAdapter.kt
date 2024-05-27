package com.example.projecta2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projecta2.R
import com.example.projecta2.model.Reservation

class MypageAdapter(private val reservationList: List<Reservation>) :
    RecyclerView.Adapter<MypageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.my_page_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (_, center, user, reservationTime) = reservationList[position]
        holder.centerName.text = center.name // 가정: FitnessCenter에 getName() 메소드가 있음
        holder.reservationTime.text = reservationTime
    }

    override fun getItemCount(): Int {
        return reservationList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var centerName: TextView
        var reservationTime: TextView

        init {
            centerName = itemView.findViewById(R.id.tvMyCenterName)
            reservationTime = itemView.findViewById<TextView>(R.id.tvMyReservationTime)
        }
    }
}
