package com.example.projecta2.model

import com.google.gson.annotations.SerializedName

data class Reservation(
    var id: Long = 0,
    var center: FitnessCenter,
    var user: User,
    var reservationTime: String
)
