package com.example.projecta2.View

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projecta2.Dao.UserDB
import com.example.projecta2.Entity.UserInfo
import com.example.projecta2.R
import com.example.projecta2.model.User
import com.example.projecta2.util.DialogHelper
import com.example.projecta2.util.RetrofitInstance
import com.example.projecta2.util.getUserObject
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var login: Button
    private lateinit var btnJoinPage: Button
    private lateinit var btnGoogleLogin: Button
    private lateinit var userEmailTextView: EditText
    private lateinit var userPwTextView: EditText
    private lateinit var email: String
    private lateinit var password: String

    // 구글 로그인 관련
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var resultLauncher: ActivityResultLauncher<Intent>

    // Room Database instance
    lateinit var db: UserDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        db = DatabaseInitializer.initDatabase(this)

        //초기화 관련
        // Room 데이터베이스 초기화
        deleteAllUsers()
        // 세션 초기화
        SessionManager.clearSession(this)

        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            Log.d("login", "이미 로그인 되어있음")
        } else {
            Log.d("login not yet", "로그인 되어있지 않음")
        }

        // 구글 로그인 초기화
        setResultSignUp()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail().requestProfile().build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // 뷰 바인딩
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin)
        btnJoinPage = findViewById(R.id.btnJoinPage)
        userEmailTextView = findViewById(R.id.userEmailTextView)
        userPwTextView = findViewById(R.id.userPwTextView)
        login = findViewById(R.id.login)

        // 로그인 버튼 클릭 리스너
        login.setOnClickListener {
            email = userEmailTextView.text.toString()
            password = userPwTextView.text.toString()
            SessionManager.saveUserEmail(this, email)
            signIn(email, password)
        }

        // 회원가입 페이지 이동
        btnJoinPage.setOnClickListener {
            val intent = Intent(applicationContext, JoinActivity::class.java)
            startActivity(intent)
        }

        // 구글 로그인 버튼 클릭 리스너
        btnGoogleLogin.setOnClickListener {
            googleSignIn()
        }
    }

    // 데이터베이스에서 모든 사용자 삭제
    private fun deleteAllUsers() {
        Thread {
            db.getDao().deleteAllUsers()
        }.start()
    }



    // 구글 로그인
    private fun googleSignIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    // 구글 로그인 결과 처리
    private fun setResultSignUp() {
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val task: Task<GoogleSignInAccount> =
                        GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    handleSignInResult(task)
                }
            }
    }

    // 구글 로그인 결과 핸들링
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            checkEmailAndSignUpGoogle(account)
        } catch (e: ApiException) {
            Log.w("GoogleLoginFailure", "signInResult:실패 code=${e.statusCode}", e)
        }
    }

    // 서버에 email을 확인하고 회원가입하거나 로그인하는 함수
    private fun checkEmailAndSignUpGoogle(account: GoogleSignInAccount) {
        val userService = RetrofitInstance.userService
        val googleEmail = account.email.toString() // 구글 이메일 가져오기
        userService.inquiryEmail(googleEmail).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) { // 서버에 email 중복이 안됨 => 회원가입 가능
                    //email, 이름 => 구글 계정 정보 사용
                    val userObj = User(email = account.email.toString(), name = account.displayName.toString())
                    // 서버에 회원가입 요청
                    userJoin(userObj)
                } else {
                    // 서버에 해당 이메일이 이미 등록되어 있는 경우 => 로그인 처리
                    loginUser(googleEmail)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Check Email", "Error: ${t.message}", t)
                // 서버 통신 실패 처리
                DialogHelper.showMessageDialog(
                    this@LoginActivity,
                    "통신 실패",
                    "서버와 통신이 실패했습니다.\n연결을 확인해주세요."
                )
            }
        })
    }

    // 서버에 회원가입 요청하는 함수
    private fun userJoin(user: User) {
        val userService = RetrofitInstance.userService
        userService.join(user).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // 회원가입 성공
                    loginUser(user.email) // 로그인 처리
                    Log.d("구글회원가입 완료", "구글 회원가입 완료")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Request Failed", "Error: ${t.message}", t)
                // 서버 통신 실패 처리
                DialogHelper.showMessageDialog(
                    this@LoginActivity,
                    "통신 실패",
                    "서버와 통신이 실패했습니다.\n연결을 확인해주세요."
                )
            }
        })
    }

    // 서버에 등록된 이메일로 로그인하는 함수
    private fun loginUser(email: String) {
        val userService = RetrofitInstance.userService
        userService.getUserInfo(email).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body() // 서버에서 받은 유저 정보

                    // 내장 DB에 유저 정보 저장
                    CoroutineScope(Dispatchers.IO).launch {
                        user?.let {
                            db.getDao().insertUser(it.toUserInfo())
                        }
                    }

                    // account.email이 null이 아닐 때에만 세션에 이메일 저장
                    SessionManager.saveUserEmail(
                        this@LoginActivity,
                        email
                    )

                    // 홈 화면으로 이동
                    val intent = Intent(applicationContext, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // 서버에서 유저 정보를 가져오지 못한 경우
                    DialogHelper.showMessageDialog(
                        this@LoginActivity,
                        "회원 정보 가져오기 실패",
                        "서버에서 회원 정보를 가져오지 못했습니다."
                    )
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("Get User Info", "Error: ${t.message}", t)
                // 서버 통신 실패 처리
                DialogHelper.showMessageDialog(
                    this@LoginActivity,
                    "통신 실패",
                    "서버와 통신이 실패했습니다.\n연결을 확인해주세요."
                )
            }
        })
    }


    // 일반 로그인
    private fun signIn(email: String, password: String) {
        val userService = RetrofitInstance.userService
        userService.signIn(email, password).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        // 사용자 정보를 Room 데이터베이스에 저장하고 저장된 정보를 로그로 출력
                        Thread {
                            val userInfo = UserInfo(
                                Id = user.id,
                                email = user.email,
                                name = user.name,
                                password = password,
                                phoneNumber = user.phoneNumber,
                                gender = user.gender,
                                address = user.address,
                                joinDate = user.joinDate,
                                role = user.role,
                                birthDate = user.birthDate
                            )

                            db.getDao().insertUser(userInfo)

                            // 저장된 비밀번호를 가져와서 로그로 출력
                            val stUser = db.getDao().getUserInfoObj(email)

                            Log.d("StoredUserInfo", "${stUser}")

                            runOnUiThread {
                                val intent = Intent(applicationContext, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }.start()
                    } ?: run {
                        Log.e("ResponseError", "null 사용자 객체를 받았습니다.")
                    }
                } else {
                    Log.e("ResponseError", "코드: ${response.code()}, 서버 연결 실패")
                    DialogHelper.showMessageDialog(
                        this@LoginActivity,
                        "로그인 실패",
                        "회원 정보가 없습니다.\n이메일과 비밀번호를 확인해주세요."
                    )
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("RequestFailed", "오류: ${t.message}", t)
            }
        })
    }
    // 화면을 터치할 때 키보드를 숨기는 기능입니다.
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null && ev != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            currentFocus!!.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }
}

