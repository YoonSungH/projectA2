package com.example.projecta2.View

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.projecta2.R
import com.example.projecta2.util.DialogHelper
import java.util.*
import kotlin.properties.Delegates

class ReservationActivity : AppCompatActivity() {

    private var selectedDate: String? = null
    private var centerId by Delegates.notNull<Long>()
    private var userId by Delegates.notNull<Long>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)


        //center, user Id 받아서 가져옴 => 사용준비완료
        centerId = intent.getLongExtra("centerId", 0L) // Long 타입으로 받음
        userId = intent.getLongExtra("userId", 0L) // Long 타입으로 받음

        Log.d("사용준비!!", "센터아이디 => ${centerId} // 유저아이디 => ${userId}")
        
        // 예약하기 버튼 클릭 시 이벤트 처리
        val btnReserve = findViewById<Button>(R.id.btnReserve)
        btnReserve.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, dayOfMonth ->
                selectedDate = "$selectedYear/${selectedMonth + 1}/$dayOfMonth"
                showReservationConfirmationDialog(selectedDate!!)
            },
            year,
            month,
            dayOfMonth
        )
        datePickerDialog.show()
    }

    private fun showReservationConfirmationDialog(reservationDate: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("예약 확인")
        val message = "예약을 진행하시겠습니까?\n\n예약 날짜: $reservationDate"
        builder.setMessage(message)
        Log.d("예약일", "${reservationDate}")
        builder.setPositiveButton("예약") { dialog, _ ->
            DialogHelper.showMessageDialog(this, "예약 확인", "예약이 완료되었습니다.\n\n예약 날짜: $reservationDate")

            //예약완료시 마이페이지로 이동
            val intent = Intent(this, MyTicketActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

}
