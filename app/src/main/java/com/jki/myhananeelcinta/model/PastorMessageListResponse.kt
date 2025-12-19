package com.jki.myhananeelcinta.model

data class PastorMessageListResponse(
    var messages: ArrayList<PastorMessage>
) {
    constructor() : this(arrayListOf())
}
