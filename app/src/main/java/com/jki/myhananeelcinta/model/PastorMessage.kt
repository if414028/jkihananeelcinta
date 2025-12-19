package com.jki.myhananeelcinta.model

import android.os.Parcel
import android.os.Parcelable

data class PastorMessage(

    var id: Int = 0,
    var date: Long = 0,
    var title: String? = "",
    var messages: String? = "",
    var writer: String? = ""

): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    constructor() : this(0, 0, "", "", "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeLong(date)
        parcel.writeString(title)
        parcel.writeString(messages)
        parcel.writeString(writer)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PastorMessage> {
        override fun createFromParcel(parcel: Parcel): PastorMessage {
            return PastorMessage(parcel)
        }

        override fun newArray(size: Int): Array<PastorMessage?> {
            return arrayOfNulls(size)
        }
    }
}
