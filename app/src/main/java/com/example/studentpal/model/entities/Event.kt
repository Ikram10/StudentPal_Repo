package com.example.studentpal.model.entities

import android.os.Parcel
import android.os.Parcelable

/**
 * The Event data class will hold all the information about the Event
 * Event class implements the Parcelable interface allowing Event instances to be written and restored from a parcel
 * This interface allows Event objects to be sent across activities with intents
 *
 * Code was adapted from Denis Panjuta's trello clone (Panjuta, 2021)
 * @see[com.example.studentpal.common.References]
 *
 */
data class Event (
    val name: String = "",
    val image: String = "",
    val createBy: String = "", // name of the user who created the event
    val assignedTo: ArrayList<String> = ArrayList(), //list of users the event is assigned to
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

    companion object CREATOR : Parcelable.Creator<Event> {

        override fun createFromParcel(parcel: Parcel): Event {
            return Event(parcel)
        }

        override fun newArray(size: Int): Array<Event?> {
            return arrayOfNulls(size)
        }
    }
}
