package com.example.wondrobe.data

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable

data class Clothes(
    var userId: String? = null,
    var imageUrl: String? = null,
    var title: String? = null,
    var typeClothes: String? = null,
    var date: String? = null,
    var processedImage: Bitmap? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Bitmap::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(imageUrl)
        parcel.writeString(title)
        parcel.writeString(typeClothes)
        parcel.writeString(date)
        parcel.writeParcelable(processedImage, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Clothes> {
        override fun createFromParcel(parcel: Parcel): Clothes {
            return Clothes(parcel)
        }

        override fun newArray(size: Int): Array<Clothes?> {
            return arrayOfNulls(size)
        }
    }
}
