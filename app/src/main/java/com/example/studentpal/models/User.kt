package com.example.studentpal.models

import android.os.Parcel
import android.os.Parcelable

// User data class implements the Parcelable interface to allow user objects to be sent across activities with intents
data class User (
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val dateJoined: String = "",
    val image : String = "",
    val status: String = "Available",
    val fcmToken : String = "" ): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(name)
        writeString(email)
        writeString(dateJoined)
        writeString(image)
        writeString(status)
        writeString(fcmToken)
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