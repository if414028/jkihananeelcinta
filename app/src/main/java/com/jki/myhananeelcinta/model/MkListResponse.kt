package com.jki.myhananeelcinta.model

data class MkListResponse(
    var mkList: ArrayList<Mk>
) {
    constructor() : this(arrayListOf())
}
