package com.jki.hananeelcinta.model

data class Announcement(
    var id: Int,
    var title: String,
    var date: Long,
    var desc: String,
    var infoUrl: String,
    var imageUrl: String
) {
    constructor() : this(0, "", 0L, "", "", "")
}
