package com.example.projecta2.model

import com.example.projecta2.Entity.UserInfo
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.io.Serial
import java.time.LocalDate
import java.util.Date

@Serializable
data class User(

    var id: Long = 0,
    var name: String = "",
    @SerializedName("email")
    var email: String = "",
    @SerializedName("password")
    var password: String = "",
    var phoneNumber: String = "",
    var gender: String = "",
    var address: String = "",
    var birthDate: String = "",
    var joinDate: String = "",
    var role: List<String> = emptyList()

) {
    constructor(id: Long) : this(id, "", "", "", "", "", "", "", "", emptyList())

    public fun toUserInfo(): UserInfo {
        return UserInfo(
            Id = this.id,
            name = this.name,
            email = this.email,
            password = this.password,
            phoneNumber = this.phoneNumber,
            gender = this.gender,
            address = this.address,
            birthDate = this.birthDate,
            joinDate = this.joinDate,
            role = this.role
        )
    }
}



