package com.jki.hananeelcinta.model

data class PrayerRequest(
    var id: String,
    var requesterId: String,
    var requesterName: String,
    var prayType: String,
    var prayDesc: String,
    var handlerId: String,
    var handlerName: String,
    var status: String
) {
    constructor() : this("", "", "", "", "", "", "", "")
}

enum class PrayType(val type: String) {
    VISITATION("Kunjungan"),
    PRAY_SUPPORT("Bantuan Doa"),
    COUNSELING("Konseling")
}

enum class Status(val status: String) {
    OPEN("Open"),
    IN_PROGRESS("Dalam Proses"),
    DONE("Selesai")
}
