package com.jki.myhananeelcinta

import android.app.Application
import com.jki.myhananeelcinta.util.UserConfiguration

class HancinApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        UserConfiguration.getInstance().initSharedPreference(applicationContext)
    }
}