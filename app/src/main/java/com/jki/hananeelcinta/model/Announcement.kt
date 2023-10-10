package com.jki.hananeelcinta.model

import android.os.Parcel
import android.os.Parcelable

data class Announcement(
    var id: Int = 0,
    var title: String? = "",
    var date: Long = 0L,
    var desc: String? = "",
    var infoUrl: String? = "",
    var imageUrl: String? = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    constructor() : this(0, "", 0L, "", "", "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeLong(date)
        parcel.writeString(desc)
        parcel.writeString(infoUrl)
        parcel.writeString(imageUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Announcement> {
        override fun createFromParcel(parcel: Parcel): Announcement {
            return Announcement(parcel)
        }

        override fun newArray(size: Int): Array<Announcement?> {
            return arrayOfNulls(size)
        }
    }

}
