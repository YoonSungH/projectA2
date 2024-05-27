package com.example.projecta2.View

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.projecta2.R
import com.example.projecta2.model.Reservation
import com.example.projecta2.util.DialogHelper.showConfirmationDialog
import com.example.projecta2.util.RetrofitInstance
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MyTicketAdapter(
    private var reservationList: MutableList<Reservation>,
    private val onDeleteListener: OnDeleteListener
) : RecyclerView.Adapter<MyTicketAdapter.MyViewHolder>(), DatePickerDialog.OnDateSetListener {

    private var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context = parent.context
        val itemView = LayoutInflater.from(context).inflate(R.layout.my_ticket_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val reservation = reservationList[position]

        holder.tvCenterName.text = reservation.center.name
        holder.tvAddress.text = reservation.center.address
        holder.tvReservationDate.text = reservation.reservationTime
        val reservationService = RetrofitInstance.reservationService

        holder.btnTicketUsed.setOnClickListener {
            reservationService.reservationUsed(reservation.id)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            reservationList.remove(reservation)
                            notifyDataSetChanged()
                            onDeleteListener.onDelete()
                        } else {
                            // Handle unsuccessful response
                        }
                    }
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        // Handle failure
                    }
                })
        }

        holder.btnReservationCancel.setOnClickListener {
            showConfirmationDialog(
                context = context!!,
                title = "예약 취소",
                message = "정말로 예약을 취소하시겠습니까?",
                onPositiveClick = {
                    reservationService.reservationCancel(reservation.id)
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                if (response.isSuccessful) {
                                    reservationList.remove(reservation)
                                    notifyDataSetChanged()
                                    onDeleteListener.onDelete()
                                } else {
                                    // Handle unsuccessful response
                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                // Handle failure
                            }
                        })
                }
            )
        }
    }

    override fun getItemCount() = reservationList.size

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnTicketUsed: Button = itemView.findViewById(R.id.btnTicketUsed)
        val btnReservationCancel: Button = itemView.findViewById(R.id.btnReservationCancel)
        val tvCenterName: TextView = itemView.findViewById(R.id.tvCenterName)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvReservationDate: TextView = itemView.findViewById(R.id.tvReservationDate)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val monthFormatted = String.format("%02d", month + 1) // 월은 0부터 시작하므로 +1 해줌
        val dayFormatted = String.format("%02d", dayOfMonth)
        val selectedDate = "$year-$monthFormatted-$dayFormatted"
        Log.d("넘어온 날짜", "${selectedDate}")
        setSelectedDate(selectedDate)
    }

    fun setSelectedDate(date: String) {
        filterReservationsByDate(date)
    }

    private fun filterReservationsByDate(date: String?) {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

        val filteredReservations = if (date.isNullOrEmpty()) {
            // 날짜가 선택되지 않은 경우, 오늘 날짜를 기준으로 필터링합니다.
            reservationList.filter { it.reservationTime.startsWith(todayDate) }
        } else {
            // 선택된 날짜가 있으면 해당 날짜로 필터링합니다.
            reservationList.filter { it.reservationTime.startsWith(date) }
        }
        updateReservationList(filteredReservations.toMutableList())
    }

    fun updateReservationList(newReservationList: MutableList<Reservation>)
    {
        reservationList.clear()
        reservationList.addAll(newReservationList)
        notifyDataSetChanged()
    }

    interface OnDeleteListener {
        fun onDelete()
    }
}
