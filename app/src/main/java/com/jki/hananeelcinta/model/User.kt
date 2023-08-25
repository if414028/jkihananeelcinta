package com.jki.hananeelcinta.model

data class User(
    var id: String,
    var email: String,
    var username: String,
    var password: String,
    var fullName: String,
    var idNumber: Long,
    var gender: String,
    var placeOfBirth: String,
    var dateOfBirth: String,
    var phoneNumber: String,
    var address: String,
    var addressLat: Long,
    var addressLong: Long,
    var bloodType: String,
    var lastEducation: String,
    var job: String,
    var waterBaptism: Boolean,
    var waterBaptisteryChurch: String,
    var waterBaptisteryDate: String,
    var holySpiritBaptism: Boolean,
    var churchOrigin: String,
    var reasonToMovingChurch: String,
    var married: Boolean,
    var fatherFullName: String,
    var motherFullName: String,
    var statusInFamily: String,
    var photoImageUrl: String,
    var role: String
) {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        0L,
        "",
        "",
        "",
        "",
        "",
        0L,
        0L,
        "",
        "",
        "",
        false,
        "",
        "",
        false,
        "",
        "",
        false,
        "",
        "",
        "",
        "",
        ""
    )
}

enum class Gender(val gender: String) {
    MALE("Laki-laki"),
    FEMALE("Perempuan")
}

enum class BloodType(val bloodType: String) {
    A("A"),
    B("B"),
    AB("AB"),
    O("O")
}

enum class Education(val education: String) {
    SD("SD"),
    SMP("SMP"),
    SMA("SMA"),
    DIPLOMA("D1, D2, D3"),
    S1("S1"),
    S2("S2"),
    S3("S3"),
    OTHER("Lainnya")
}

enum class Job(val job: String) {
    EMPLOYEE("Pegawai Swasta"),
    GOVERNMENT_EMPOLYEES("Pegawai Negeri Sipil"),
    PROFESSION("Profesi (Dokter, Pengacara, dll)"),
    SERVANT("Fulltimer Pelayanan"),
    OTHER("Lainnya")
}

enum class YesNoQuestion(val yesNoQuestion: String) {
    YES("Sudah"),
    NO("Belum")
}

enum class HasMarriedQuestion(val hasMarriedQuestion: String) {
    YES("Sudah Menikah"),
    NO("Belum Menikah")
}

enum class FamilyStatus(val familyStatus: String) {
    HEAD_OF_FAMILY("Kepala Keluarga"),
    WIFE("Istri"),
    CHILD("Anak"),
    WIDOW("Janda"),
    WIDOWER("Duda"),
    SINGLE("Single - Belum Menikah")
}