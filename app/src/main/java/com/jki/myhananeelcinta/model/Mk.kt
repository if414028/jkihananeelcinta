package com.jki.myhananeelcinta.model

data class Mk(
    var id: Int,
    var contact: String,
    var day: String,
    var desc: String,
    var location: String,
    var pic: String,
    var time: String
) {
    constructor() : this(0, "", "", "", "", "", "")
}
