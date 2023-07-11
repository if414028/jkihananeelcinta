package com.jki.hananeelcinta.model

data class MkListResponse(
    var mkList: ArrayList<Mk>
) {
    constructor() : this(arrayListOf())
}
