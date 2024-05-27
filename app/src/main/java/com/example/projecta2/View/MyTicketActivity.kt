package com.example.projecta2.View

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projecta2.Entity.UserInfo
import com.example.projecta2.R
import com.example.projecta2.model.Reservation
import com.example.projecta2.model.Result
import com.example.projecta2.util.RetrofitInstance
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MyTicketActivity : AppCompatActivity(), MyTicketAdapter.OnDeleteListener {

    private lateinit var userInfo: UserInfo

    private lateinit var ticketPageUserName: TextView
    private lateinit var tvMyTicketCount: TextView
    private lateinit var tvTodayString: TextView
    private lateinit var tvReservationDateText: TextView
    private lateinit var ticketUseRecycler: RecyclerView
    private lateinit var todayReservationList: MutableList<Reservation>
    private lateinit var adapter: MyTicketAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_ticket)

        tvMyTicketCount = findViewById(R.id.tvMyTicketCount)
        tvTodayString = findViewById(R.id.tvTodayString)
        ticketUseRecycler = findViewById(R.id.ticketUseRecycler)
        tvReservationDateText = findViewById(R.id.tvReservationDateText)

        userInfo = intent.getParcelableExtra<UserInfo>("userInfo")!!

        ticketPageUserName = findViewById(R.id.ticketPageUserName)
        ticketPageUserName.text = userInfo.name

        setupRecyclerView()

        fetchUserReservations()

        val homeImageView: ImageView = findViewById(R.id.homeImageViewUserEdit4)
        homeImageView.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        val mapFloatingActionButton: FloatingActionButton = findViewById(R.id.mapFabUserEdit4)
        mapFloatingActionButton.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        val myPageImageView: ImageView = findViewById(R.id.myPageImageView4)
        myPageImageView.setOnClickListener {
            startActivity(Intent(this, MyPageActivity::class.java))
        }

        tvReservationDateText.setOnClickListener {
            showDatePicker()
        }
    }

    private fun fetchUserReservations() {
        val reservationService = RetrofitInstance.reservationService

        reservationService.getUserReservations(userInfo.Id).enqueue(object :
            Callback<Result<Reservation>> {
            override fun onResponse(
                call: Call<Result<Reservation>>,
                response: Response<Result<Reservation>>
            ) {
                if (response.isSuccessful) {
                    val result: Result<Reservation>? = response.body()
                    if (result != null) {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val todayDate = dateFormat.format(Date())
                        tvTodayString.text = "(" + todayDate + ")"
                        todayReservationList = result.data.toMutableList()
                        adapter = MyTicketAdapter(todayReservationList, this@MyTicketActivity)
                        tvMyTicketCount.text = todayReservationList.size.toString()
                        ticketUseRecycler.adapter = adapter

                    } else {
                        Log.e(
                            "Reservation Error",
                            "Failed to get reservation list. Response body is null."
                        )
                    }
                } else {
                    Log.e(
                        "Reservation Error",
                        "Failed to get reservation list. Code: ${response.code()}"
                    )
                }
            }

            override fun onFailure(call: Call<Result<Reservation>>, t: Throwable) {
                Log.e(
                    "Reservation Error",
                    "Failed to get reservation list. Error: ${t.message}",
                    t
                )
            }
        })
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val monthFormatted = String.format("%02d", selectedMonth + 1)
                val dayFormatted = String.format("%02d", selectedDayOfMonth)
                val selectedDate = "$selectedYear-$monthFormatted-$dayFormatted"
                tvTodayString.text = "(" + "${selectedDate}" + ")"
                adapter.setSelectedDate(selectedDate)
            },
            year,
            month,
            dayOfMonth
        )
        datePickerDialog.show()
    }

    private fun setupRecyclerView() {
        ticketUseRecycler.layoutManager = LinearLayoutManager(this)
    }

    override fun onDelete() {
        tvMyTicketCount.text = todayReservationList.size.toString()
    }
}
