package com.example.projecta2.util

import android.content.Context
import androidx.appcompat.app.AlertDialog

object DialogHelper {
    fun showMessageDialog(context: Context, title: String, message: String, onPositiveClick: (() -> Unit)? = null) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("확인") { dialog, _ ->
                // 확인 버튼을 클릭하면 아무 작업 없이 대화 상자를 닫습니다.
                dialog.dismiss()
                // 추가된 코드: 확인 버튼 클릭 시 실행할 동작을 수행합니다.
                onPositiveClick?.invoke()
            }

        val dialog = builder.create()
        dialog.show()
    }

    // 확인과 취소 버튼이 있는 대화 상자
    fun showConfirmationDialog(context: Context, title: String, message: String, onPositiveClick: (() -> Unit)? = null,
                               onNegativeClick: (() -> Unit)? = null) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
            .setPositiveButton("확인") { dialog, _ ->
                // 확인 버튼을 클릭하면 아무 작업 없이 대화 상자를 닫습니다.
                dialog.dismiss()
                // 추가된 코드: 확인 버튼 클릭 시 실행할 동작을 수행합니다.
                onPositiveClick?.invoke()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
                onNegativeClick?.invoke()
            }

        val dialog = builder.create()
        dialog.show()
    }

    // 삭제 확인
    fun showDeleteConfirmationDialog(context: Context,
                                     title: String,
                                     message: String,
                                     onDeleteConfirmed: () -> Unit,
                                     onCancel: () -> Unit) {
        showConfirmationDialog(context, title, message,
            onPositiveClick = {
                onDeleteConfirmed.invoke() // 삭제 확인 버튼 클릭 시 삭제를 수행합니다.
            },
            onNegativeClick = onCancel
        )
    }
}

