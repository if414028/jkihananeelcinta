package com.jki.myhananeelcinta.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.jki.myhananeelcinta.model.User

class UserConfiguration {

    private lateinit var sharedPreferences: SharedPreferences

    val SP_NAME = "HANCIN_SP"

    private val USER_DATA = "user_data"
    private val USER_ID = "user_id"

    companion object {
        private val ourInstance = UserConfiguration()
        fun getInstance(): UserConfiguration {
            return ourInstance
        }
    }

    fun initSharedPreference(context: Context) {
        sharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }

    fun setUserData(user: User) {
        val gson = Gson()
        val userJson = gson.toJson(user)
        val editor = sharedPreferences.edit()
        editor.putString(USER_DATA, userJson)
        editor.apply()
    }

    fun getUserData(): User? {
        val gson = Gson()
        val userJson = sharedPreferences.getString(USER_DATA, null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }
    }

    fun setUserId(userId: String) {
        val editor = sharedPreferences.edit()
        editor.putString(USER_ID, userId)
        editor.apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString(USER_ID, null)
    }
}