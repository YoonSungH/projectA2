package com.example.projecta2.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projecta2.Entity.UserInfo
import com.example.projecta2.R
import com.example.projecta2.View.SessionManager
import com.example.projecta2.model.Review
import com.example.projecta2.util.RetrofitInstance
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewAdapter(
    private var reviews: List<Review>,
    private val userInfo: UserInfo, // 추가
    private val onDeleteClickListener: (Long) -> Unit
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val authorTextView: TextView = itemView.findViewById(R.id.reviewAuthor)
        private val ratingTextView: TextView = itemView.findViewById(R.id.reviewRating)
        private val contentTextView: TextView = itemView.findViewById(R.id.reviewContent)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        // 현재 사용자의 이메일을 가져옵니다.
        val currentUserEmail = SessionManager.getUserEmail(itemView.context)

        //여기에다가 UserInfo에 들어있는 userId 만 가지고와서 표현만 하면됨
//        fun bind(review: Review) {
//            // 작성자 ID 설정
//            authorTextView.text = review.userName.toString()
//
//            // 평점 설정 (사용되지 않았을 때는 생략 가능)
//            ratingTextView.text = review.rating.toString()
//
//            // 리뷰 내용 설정
//            contentTextView.text = review.reviewText
//
//            deleteButton.setOnClickListener {
//                // 리뷰의 id를 삭제 버튼 클릭 리스너에 전달
//                onDeleteClickListener(review.id)
//            }
//
//        }


        fun bind(review: Review) {
            // 작성자 ID 설정
            authorTextView.text = review.userName.toString()

            // 평점 설정 (사용되지 않았을 때는 생략 가능)
            ratingTextView.text = review.rating.toString()

            // 리뷰 내용 설정
            contentTextView.text = review.reviewText

            // 현재 사용자의 userId와 글쓴이의 userId를 비교하여 같으면 삭제 버튼을 보여줍니다.
            if (review.userId == userInfo.Id) {
                deleteButton.visibility = View.VISIBLE
                deleteButton.setOnClickListener {
                    // 리뷰의 id를 삭제 버튼 클릭 리스너에 전달
                    onDeleteClickListener(review.id)
                }
            } else {
                // 현재 사용자가 글쓴이가 아니면 삭제 버튼을 숨깁니다.
                deleteButton.visibility = View.GONE
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.reviewlist_item, parent, false)
        return ReviewViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    fun setReviews(reviews: List<Review>) {
        this.reviews = reviews
        notifyDataSetChanged()
    }


    fun deleteReview(id: Long, userId: Long, onDeleteCompleted: (Boolean) -> Unit) {
        // RetrofitInstance에서 ReviewService 인스턴스를 가져옵니다.
        val reviewService = RetrofitInstance.reviewService

        // 리뷰 삭제 요청을 보냅니다.
        val call = reviewService.deleteReview(id, userId)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // 삭제 성공 시 true를 전달합니다.
                    Log.d(">>>>>", "deleteReview성공을 리턴")
                    onDeleteCompleted(true)
                } else {
                    // 삭제 실패 시 false를 전달합니다.
                    Log.d(">>>>>", "deleteReview삭제 실패리턴함")
                    onDeleteCompleted(false)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 네트워크 오류 등으로 요청 실패 시의 동작을 정의합니다.
                onDeleteCompleted(false)
            }
        })
    }

    private fun removeItem(id: Long) {
        val mutableList = reviews.toMutableList()
        val iterator = mutableList.iterator()
        while (iterator.hasNext()) {
            val review = iterator.next()
            if (review.id == id) {
                iterator.remove()
                setReviews(mutableList)
                break
            }
        }
    }

}