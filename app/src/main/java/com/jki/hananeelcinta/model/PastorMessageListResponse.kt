package com.jki.hananeelcinta.model

data class PastorMessageListResponse(
    var messages: ArrayList<PastorMessage>
) {
    constructor() : this(arrayListOf())
}
