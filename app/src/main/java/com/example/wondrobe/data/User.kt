package com.example.wondrobe.data

import android.os.Parcel
import android.os.Parcelable

data class User(
    val uid: String?,
    val email: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val biography: String? = null,
    val password: String? = null,
    val profileImage: String? = null,
    val bannerImage: String? = null,
    val isAdmin: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    var isFollowing: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(email)
        parcel.writeString(username)
        parcel.writeString(firstName)
        parcel.writeString(biography)
        parcel.writeString(password)
        parcel.writeString(profileImage)
        parcel.writeString(bannerImage)
        parcel.writeByte(if (isAdmin) 1 else 0)
        parcel.writeInt(followersCount)
        parcel.writeInt(followingCount)
        parcel.writeByte(if (isFollowing) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}
