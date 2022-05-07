package com.example.studentpal.model.entities

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
// User entity
// User data class implements the Parcelable interface to allow user objects to be sent across activities with intents
data class User (
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val dateJoined: String = "",
    val image : String = "",
    val status: String = "",
    val fcmToken : String = "",
    val coverImage : String = "",
    var numFriends : Int = 0,
    val username: String = "",
    val selected: Boolean = false
): Parcelable {



    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readBoolean()
    )

    override fun describeContents() = 0

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(name)
        writeString(email)
        writeString(dateJoined)
        writeString(image)
        writeString(status)
        writeString(fcmToken)
        writeString(coverImage)
        writeInt(numFriends)
        writeString(username)
        writeBoolean(selected)
    }

    companion object CREATOR : Parcelable.Creator<User> {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }


}