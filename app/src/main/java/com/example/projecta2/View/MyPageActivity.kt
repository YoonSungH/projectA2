package com.example.projecta2.View

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projecta2.Dao.UserDB
import com.example.projecta2.R
import com.example.projecta2.model.User
import com.example.projecta2.adapter.MypageAdapter // 어댑터 임포트 추가
import com.example.projecta2.model.FitnessCenter
import com.example.projecta2.model.Reservation
import com.example.projecta2.model.Result
import com.example.projecta2.util.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.properties.Delegates

class MyPageActivity : AppCompatActivity() {

    // Room Database 인스턴스
    private lateinit var db: UserDB
    private var userName: String? = null
    private var userId by Delegates.notNull<Long>()
    private lateinit var tvUserName: TextView
    private lateinit var tvTotalCounting: TextView
    private lateinit var recyclerView: RecyclerView // 리사이클러뷰 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        // 데이터베이스 사용 준비
        db = DatabaseInitializer.initDatabase(this)
        tvUserName = findViewById<TextView>(R.id.MyPageUserName)
        tvTotalCounting = findViewById<TextView>(R.id.tvTotalCounting)

        // 사용자 이메일 가져오기
        val email = SessionManager.getUserEmail(this)
        if (email != null) {
            Log.d("Email Log", email)
            // 코루틴 사용하여 DB 작업 실행
            lifecycleScope.launch {
                val stUser = withContext(Dispatchers.IO) {
                    db.getDao().getUserInfoObj(email)
                }
                userName = stUser?.name
                userId = stUser?.Id!!
                tvUserName.text = userName
                Log.d("로그인 상태", "현재 로그인된 ${userName}님 고유 id 는 ${userId} 입니다")

                //카운팅 세팅
                setVisitCounting()
            }
        } else {
            Log.d("Email Log", "Email is null")
        }

        // 홈으로 이동
        val homeLinearLayout = findViewById<LinearLayout>(R.id.home_linear_layout)
        homeLinearLayout.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        // 맵 보기
        val showMap = findViewById<FloatingActionButton>(R.id.favorite)
        showMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        // 사용자 정보 수정 페이지 이동
        val editInfoCard = findViewById<CardView>(R.id.editInfo)
        editInfoCard.setOnClickListener {
            val intent = Intent(this, UserEditActivity::class.java)
            startActivity(intent)
        }

        // 리사이클러뷰 설정
        //recyclerView = findViewById<RecyclerView>(R.id.myPageRecycler) // 리사이클러뷰 ID 할당
        //recyclerView.layoutManager = LinearLayoutManager(this) // 리사이클러뷰 레이아웃 매니저 설정
        // 여기에 어댑터 설정 코드 추가. 예시를 위한 가상의 데이터 리스트와 어댑터 설정을 진행합니다.

        // 실제 데이터 리스트를 DB나 서버에서 가져와야 합니다.
        // val exampleList = listOf()

        // 리사이클러뷰 어댑터 설정
        //recyclerView.adapter = MypageAdapter(exampleList) // 어댑터 설정
    }

    // 총 이용횟수 카운팅 메소드
    private fun setVisitCounting() {
        //총 이용횟수 카운팅
        val visitCountingService = RetrofitInstance.visitCountingService
        Log.d(">>>>", "${userId}")
        // 비동기적으로 요청을 수행하고 응답을 처리
        visitCountingService.getMyCounting(userId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // 응답이 성공적으로 받아졌을 때의 처리
                    val responseBody = response.body()
                    if (responseBody != null) {
                        try {
                            // 서버 응답을 문자열로 변환하고 이를 Long 형식으로 파싱하여 텍스트뷰에 설정
                            val counting = responseBody.string()
                            tvTotalCounting.text = counting
                        } catch (e: Exception) {
                            Log.e("VisitCountingError", "Failed to parse visit count.", e)
                        }
                    }
                } else {
                    // 응답이 실패했을 때의 처리
                    // response.code()를 사용하여 실패 코드를 확인할 수 있음
                    Log.e(
                        "VisitCountingError",
                        "Failed to get visit count. Code: ${response.code()}"
                    )
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 네트워크 오류 또는 서버 응답 파싱 오류 등의 실패 시 처리
                // t.message를 사용하여 실패 원인을 확인할 수 있음
                Log.e("VisitCountingError", "Failed to get visit count.", t)
            }
        })
    }
}
