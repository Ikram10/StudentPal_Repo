package com.example.studentpal.models

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.common.internal.safeparcel.SafeParcelWriter.writeStringList

/**
 * The board data class will hold all the information about the board
 * Board class implements the Parcelable interface allowing board instances to be written and restored from a parcel
 * This interface allows board objects to be sent across activities with intents
 *
 */
data class Board (
    val name: String = "",
    val image: String = "",
    // name of the user who created the board
    val createBy: String = "",
    //list of users the board is assigned to
    val assignedTo: ArrayList<String> = ArrayList(),
    var documentID: String = "",
    val dateCreated: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readString()!!,
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
        parcel.writeString(dateCreated)
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
