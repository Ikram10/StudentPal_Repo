package com.example.studentpal.models

import android.os.Parcel
import android.os.Parcelable

data class AssignedMembers (
    val id: String = "",
    val image : String = ""
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AssignedMembers> {
        override fun createFromParcel(parcel: Parcel): AssignedMembers {
            return AssignedMembers(parcel)
        }

        override fun newArray(size: Int): Array<AssignedMembers?> {
            return arrayOfNulls(size)
        }
    }

}
