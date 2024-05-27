package com.example.projecta2.View

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.projecta2.R
import com.example.projecta2.model.User
import com.example.projecta2.util.DialogHelper
import com.example.projecta2.util.RetrofitInstance
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

class JoinActivity : AppCompatActivity() {

    // UI 요소 선언
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var etBirthDate: EditText
    private lateinit var joinDate: String
    private lateinit var selectedGender: String
    private lateinit var selectedGenderEng: String

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val currentDate = LocalDateTime.now().format(formatter)

        // UI 요소 초기화
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etBirthDate = findViewById(R.id.etBirthDate)
        joinDate = currentDate // 가입 날짜 초기값 설정

        // 성별 선택 라디오 그룹 설정
        val radioGroup = findViewById<RadioGroup>(R.id.rgGender)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val radioButton = findViewById<RadioButton>(checkedId)
            selectedGender = radioButton.text.toString()
            selectedGenderEng = if (selectedGender == "남자") "male" else "female"
        }

        // 회원가입 버튼 클릭 리스너 설정
        findViewById<Button>(R.id.btnJoinPro).setOnClickListener {
            Log.d("JoinActivity", "회원가입 버튼 클릭됨")
            // 입력값으로부터 유저 객체 생성
            val user = createUserFromInput()
            // 입력값 유효성 검사 후, 이메일 중복 검사 및 회원가입
            if (validateInput(user)) {
                checkEmailAndSignUp(user)
            }
        }
    }

    // 입력값으로부터 User 객체를 생성하는 함수
    private fun createUserFromInput(): User {
        return User(
            id = 0,
            name = etName.text.toString(),
            email = etEmail.text.toString(),
            password = etPassword.text.toString(),
            phoneNumber = etPhoneNumber.text.toString(),
            gender = selectedGenderEng,
            address = "부산광역시",
            joinDate = joinDate,
            birthDate = etBirthDate.text.toString(),
            role = listOf("ROLE_USER")
        )
    }

    // 유효성 검사 함수
    private fun validateInput(user: User): Boolean {
        // 이름에 숫자가 있는지 검사
        if (!Pattern.matches("^[가-힣a-zA-Z]+$", user.name)) {
            showAlert("이름에는 숫자가 들어갈 수 없습니다.", "영문 및 한글만 입력 가능합니다.")
            etName.requestFocus()
            return false
        }

        // 아이디 형식 검사 (영문과 숫자만 허용)
        if (!Pattern.matches("^[a-zA-Z0-9]+$", user.email)) {
            showAlert("ID는 한글이 포함될 수 없습니다.", "영문 및 숫자만 입력 가능합니다.")
            etEmail.requestFocus()
            return false
        }

        // 전화번호 형식 검사 (000-0000-0000 형식)
        if (!Pattern.matches("^01([0|1|6|7|8|9])-(\\d{3,4})-(\\d{4})$", user.phoneNumber)) {
            showAlert("전화번호 양식이 올바르지 않습니다.", "ex) 010-1234-5678 \n \n 하이픈(-)을 포함 해주세요.")
            etPhoneNumber.requestFocus()
            return false
        }

        // 생년월일 형식 검사 (0000-00-00 형식)
        if (!Pattern.matches("^\\d{4}-\\d{2}-\\d{2}$", etBirthDate.text.toString())) {
            showAlert("생년월일 양식이 올바르지 않습니다.", "ex) 1996-08-09 (8자리)\n \n 하이픈(-)을 포함 해주세요.")
            etBirthDate.requestFocus()
            return false
        }

        // 모든 검사를 통과하면 true를 반환
        return true
    }

    // Alert Dialog를 표시하는 함수
    private fun showAlert(title: String, message: String) {
        AlertDialog.Builder(this).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("확인") { dialog, _ -> dialog.dismiss() }
            create()
            show()
        }
    }

    // 이메일 중복 검사 및 회원가입을 진행하는 함수
    private fun checkEmailAndSignUp(user: User) {
        val userService = RetrofitInstance.userService
        userService.inquiryEmail(user.email).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    userJoin(user)
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (response.code() == 409) {
                        DialogHelper.showMessageDialog(
                            this@JoinActivity,
                            "사용중인 email",
                            "사용중인 이메일입니다.\n변경 후, 다시 시도해 주세요."
                        )
                        etEmail.requestFocus()
                    } else {
                        DialogHelper.showMessageDialog(
                            this@JoinActivity,
                            "중복 확인 실패",
                            "이메일 중복 확인 실패: $errorBody"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Check Email", "Error: ${t.message}", t)
                DialogHelper.showMessageDialog(
                    this@JoinActivity,
                    "통신 실패",
                    "서버와 통신이 실패했습니다.\n연결을 확인해주세요."
                )
            }
        })
    }

    // 회원가입을 서버에 요청하는 함수
    fun userJoin(user: User) {
        val userService = RetrofitInstance.userService

        userService.join(user).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    DialogHelper.showMessageDialog(
                        this@JoinActivity,
                        "회원가입 성공",
                        "${etName.text.toString()}님\n회원가입에 성공했습니다."
                    ) {
                        val intent = Intent(applicationContext, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Request Failed", "Error: ${t.message}", t)
                DialogHelper.showMessageDialog(
                    this@JoinActivity,
                    "통신 실패",
                    "서버와 통신이 실패했습니다.\n연결을 확인해주세요."
                )
            }
        })
    }

    // 화면 터치 이벤트를 처리하는 함수
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // 키보드 숨김 처리
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = android.graphics.Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}


