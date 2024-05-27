package com.example.projecta2.View

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.projecta2.Entity.UserInfo
import com.example.projecta2.R
import com.example.projecta2.adapter.BannerAdapter
import com.example.projecta2.adapter.HomeAdapter
import com.example.projecta2.model.FitnessCenter
import com.example.projecta2.util.RetrofitInstance
import com.example.projecta2.util.getUserObject
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    // 뷰 변수 선언
    private lateinit var homeToMap: FloatingActionButton // 지도 보기 버튼
    private lateinit var myTicketCardView: CardView // 내 보유 일일권 카드뷰
    private lateinit var imgBannerRecyclerView: RecyclerView // 이미지 배너 리사이클러뷰
    private lateinit var autoScrollHandler: Handler // 배너 자동 스크롤을 위한 핸들러
    private lateinit var autoScrollRunnable: Runnable // 배너 자동 스크롤을 위한 실행 가능 객체
    private var autoScrollDelay: Long = 2000 // 배너 자동 스크롤 지연 시간 (2초)
    private lateinit var HomeRecycler: RecyclerView // 메인 화면 리사이클러뷰
    private lateinit var fitnessCenters: List<FitnessCenter> // 피트니스 센터 데이터 리스트

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initView() // 뷰 초기화 메서드 호출
        setupBannerRecyclerView() // 배너 리사이클러뷰 설정
        checkCenter() // 피트니스 센터 데이터 체크
        setupListeners() // 리스너 설정
    }

    private fun initView() {
        // 뷰 바인딩
        homeToMap = findViewById(R.id.homeToMap)
        myTicketCardView = findViewById(R.id.myTicketCardView)
        imgBannerRecyclerView = findViewById(R.id.imgBannerRecyclerView)
    }

    private fun setupListeners() {
        // 사용자 정보 로딩 및 이벤트 리스너 설정
        lifecycleScope.launch {
            val userInfo: UserInfo? = getUserObject(this@HomeActivity).getUserInfo()

            myTicketCardView.setOnClickListener {
                // 내 보유 일일권 클릭 이벤트 처리
                val intent = Intent(this@HomeActivity, MyTicketActivity::class.java).apply {
                    putExtra("userInfo", userInfo)
                }
                startActivity(intent)
            }
        }

        homeToMap.setOnClickListener {
            // 지도 보기 버튼 클릭 이벤트 처리
            startActivity(Intent(this, MapActivity::class.java))
        }

        // 홈으로 이동하는 LinearLayout 클릭 이벤트 처리
        findViewById<LinearLayout>(R.id.HomeToHomeLayout).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        // 마이 페이지로 이동하는 LinearLayout 클릭 이벤트 처리
        findViewById<LinearLayout>(R.id.HomeToMyPageLayout).setOnClickListener {
            startActivity(Intent(this, MyPageActivity::class.java))
        }
    }

    private fun checkCenter() {
        // 피트니스 센터 정보 요청 및 처리
        val gymService = RetrofitInstance.gymService

        gymService.getGymList().enqueue(object : Callback<List<FitnessCenter>> {
            override fun onResponse(call: Call<List<FitnessCenter>>, response: Response<List<FitnessCenter>>) {
                if (response.isSuccessful) {
                    fitnessCenters = response.body() ?: emptyList()
                    setupRecyclerView()
                } else {
                    Log.e("Response Error", "Code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<FitnessCenter>>, t: Throwable) {
                Log.e("Request Failed", "Error: ${t.message}", t)
            }
        })
    }

    private fun setupRecyclerView() {
        // 메인 화면 리사이클러뷰 설정
        HomeRecycler = findViewById(R.id.HomeRecycler)
        HomeRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        lifecycleScope.launch {
            val userInfo: UserInfo? = getUserObject(this@HomeActivity).getUserInfo()
            val adapter = HomeAdapter(fitnessCenters) { fitnessCenter ->
                // 각 항목 클릭 이벤트 처리
                val intent = Intent(this@HomeActivity, CenterDetailActivity::class.java).apply {
                    putExtra("centerId", fitnessCenter.id)
                    putExtra("centerName", fitnessCenter.name)
                    putExtra("centerPrice", fitnessCenter.dailyPassPrice)
                    putExtra("centerLocation", fitnessCenter.address)
                    putExtra("centerImageUrl", fitnessCenter.imagePath?.let { "http://10.100.103.49:8111/img/$it" })
                    putExtra("userInfo", userInfo)
                }
                startActivity(intent)
            }
            HomeRecycler.adapter = adapter
        }
    }

    private fun setupBannerRecyclerView() {
        // 배너 리사이클러뷰 설정
        val images = listOf(R.drawable.bannerimg, R.drawable.bannerimg2, R.drawable.bannerimg3)
        imgBannerRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imgBannerRecyclerView.adapter = BannerAdapter(images)

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(imgBannerRecyclerView)
    }

    override fun onResume() {
        super.onResume()
        startAutoScrollBanner() // 액티비티 재개 시 배너 자동 스크롤 시작
    }

    override fun onPause() {
        stopAutoScrollBanner() // 액티비티 일시정지 시 배너 자동 스크롤 중지
        super.onPause()
    }

    private fun startAutoScrollBanner() {
        // 배너 자동 스크롤 로직 구현
        autoScrollHandler = Handler()
        autoScrollRunnable = object : Runnable {
            override fun run() {
                var scrollPosition = (imgBannerRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                scrollPosition = (scrollPosition + 1) % (imgBannerRecyclerView.adapter?.itemCount ?: 1)
                imgBannerRecyclerView.smoothScrollToPosition(scrollPosition)
                autoScrollHandler.postDelayed(this, autoScrollDelay)
            }
        }
        autoScrollHandler.postDelayed(autoScrollRunnable, autoScrollDelay)
    }

    private fun stopAutoScrollBanner() {
        // 배너 자동 스크롤 중지
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // 화면 터치 이벤트 처리: 키보드 숨김
        if (event.action == MotionEvent.ACTION_DOWN) {
            currentFocus?.let { v ->
                if (v is android.widget.EditText) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                    v.clearFocus()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}
