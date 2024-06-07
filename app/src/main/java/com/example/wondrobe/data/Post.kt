package com.example.wondrobe.data

import android.os.Parcel
import android.os.Parcelable
import java.util.Date

data class Post(
    val postId: String?,
    val userId: String?,
    val imageUrl: String?,
    val title: String?,
    val description: String?,
    val date: Date?,
    var isSaved: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readSerializable() as? Date,
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(postId)
        parcel.writeString(userId)
        parcel.writeString(imageUrl)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeSerializable(date)
        parcel.writeByte(if (isSaved) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Post> {
        override fun createFromParcel(parcel: Parcel): Post {
            return Post(parcel)
        }

        override fun newArray(size: Int): Array<Post?> {
            return arrayOfNulls(size)
        }
    }
}
