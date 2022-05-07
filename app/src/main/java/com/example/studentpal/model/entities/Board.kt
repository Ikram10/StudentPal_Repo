package com.example.studentpal.model.entities

import android.os.Parcel
import android.os.Parcelable

/**
 * The board data class will hold all the information about the board
 * Board class implements the Parcelable interface allowing board instances to be written and restored from a parcel
 * This interface allows board objects to be sent across activities with intents
 *
 */
data class Board (
    val name: String = "",
    val image: String = "",
    val createBy: String = "", // name of the user who created the board
    val assignedTo: ArrayList<String> = ArrayList(), //list of users the board is assigned to
    var documentID: String = "",
    var eventDate: Long = 0,
    var eventDescription: String = "",
    var creatorID: String = "",
    val cardColor : String = "",
    val eventLocation : String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var eventTime: String = ""


) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString()!!
    )
    {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        parcel.writeString(name)
        parcel.writeString(image)
        parcel.writeString(createBy)
        parcel.writeStringList(assignedTo)
        parcel.writeString(documentID)
        parcel.writeLong(eventDate)
        parcel.writeString(eventDescription)
        parcel.writeString(creatorID)
        parcel.writeString(cardColor)
        parcel.writeString(eventLocation)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(eventTime)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Board> {

        override fun createFromParcel(parcel: Parcel): Board {
            return Board(parcel)
        }

        override fun newArray(size: Int): Array<Board?> {
            return arrayOfNulls(size)
        }
    }
}
