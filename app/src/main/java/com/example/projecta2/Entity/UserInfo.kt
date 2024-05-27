package com.example.projecta2.Entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//
import android.os.Parcel
import android.os.Parcelable
import com.example.projecta2.model.User

@Entity
data class UserInfo(
    @PrimaryKey val Id: Long?,
    @ColumnInfo val email: String?,
    @ColumnInfo val name: String?,
    @ColumnInfo val password: String?,
    @ColumnInfo val phoneNumber: String?,
    @ColumnInfo val gender: String?,
    @ColumnInfo val address: String?,
    @ColumnInfo val joinDate: String?,
    @ColumnInfo val role: List<String>,
    @ColumnInfo val birthDate: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList() ?: arrayListOf(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(Id)
        parcel.writeString(email)
        parcel.writeString(name)
        parcel.writeString(password)
        parcel.writeString(phoneNumber)
        parcel.writeString(gender)
        parcel.writeString(address)
        parcel.writeString(joinDate)
        parcel.writeStringList(role)
        parcel.writeString(birthDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserInfo> {
        override fun createFromParcel(parcel: Parcel): UserInfo {
            return UserInfo(parcel)
        }

        override fun newArray(size: Int): Array<UserInfo?> {
            return arrayOfNulls(size)
        }
    }

    public fun toUser(): User {
        return User(
            id = this.Id ?: 0,
            name = this.name ?: "",
            email = this.email ?: "",
            password = this.password ?: "",
            phoneNumber = this.phoneNumber ?: "",
            gender = this.gender ?: "",
            address = this.address ?: "",
            birthDate = this.birthDate ?: "",
            joinDate = this.joinDate ?: "",
            role = this.role
        )
    }
}
