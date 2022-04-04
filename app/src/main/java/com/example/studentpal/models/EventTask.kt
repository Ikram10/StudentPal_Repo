package com.example.studentpal.models

import android.os.Parcel
import android.os.Parcelable

//Each event can have multiple tasks
data class EventTask (
    var title: String = "",
    val createdBy: String = ""

        ): Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(createdBy)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EventTask> {
        override fun createFromParcel(parcel: Parcel): EventTask {
            return EventTask(parcel)
        }

        override fun newArray(size: Int): Array<EventTask?> {
            return arrayOfNulls(size)
        }
    }
}