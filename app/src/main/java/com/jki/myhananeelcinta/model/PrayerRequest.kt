package com.jki.myhananeelcinta.model

import android.os.Parcel
import android.os.Parcelable

data class PrayerRequest(
    var id: String? = "",
    var requesterId: String? = "",
    var requesterName: String? = "",
    var prayType: String? = "",
    var prayDesc: String? = "",
    var handlerId: String? = "",
    var handlerName: String? = "",
    var status: String? = "",
    var prayResult: String? = ""
) : Parcelable {
    constructor() : this("", "", "", "", "", "", "", "", "")

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun describeContents(): Int {
        return 0;
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(requesterId)
        parcel.writeString(requesterName)
        parcel.writeString(prayType)
        parcel.writeString(prayDesc)
        parcel.writeString(handlerId)
        parcel.writeString(handlerName)
        parcel.writeString(status)
        parcel.writeString(prayResult)
    }

    companion object CREATOR : Parcelable.Creator<PrayerRequest> {
        override fun createFromParcel(parcel: Parcel): PrayerRequest {
            return PrayerRequest(parcel)
        }

        override fun newArray(size: Int): Array<PrayerRequest?> {
            return arrayOfNulls(size)
        }
    }
}

enum class PrayType(val type: String) {
    VISITATION("Kunjungan"),
    PRAY_SUPPORT("Bantuan Doa"),
    COUNSELING("Konseling")
}

enum class Status(val status: String) {
    OPEN("Open"),
    IN_PROGRESS("Dalam Proses"),
    DONE("Selesai"),
    CANCELLED("Dibatalkan")
}
