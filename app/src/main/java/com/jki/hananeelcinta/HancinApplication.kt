package com.jki.hananeelcinta

import android.app.Application
import com.jki.hananeelcinta.util.UserConfiguration

class HancinApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        UserConfiguration.getInstance().initSharedPreference(applicationContext)
    }
}