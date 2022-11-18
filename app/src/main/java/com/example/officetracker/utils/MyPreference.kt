package com.example.officetracker.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences


object MyPreference {

    private const val PREF_NAME = "USER_PREF"

    fun writePrefString(context: Context,key: String,value: String) {
        val editor: SharedPreferences.Editor =
            context.getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun readPrefString(context: Context, key: String): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        return prefs.getString(key, "")
    }

    fun writePrefBool(context: Context,key: String,value: Boolean){
        val editor: SharedPreferences.Editor = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
        editor.putBoolean(key,value)
        editor.apply()
    }

    fun readPrefBool(context: Context,key: String):Boolean{
        val prefs:SharedPreferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        return prefs.getBoolean(key,false)
    }
    fun clear(context: Context){
        val settings: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        settings.edit().clear().apply()
    }
}