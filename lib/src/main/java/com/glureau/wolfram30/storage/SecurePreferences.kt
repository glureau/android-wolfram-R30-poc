package com.glureau.wolfram30.storage

/**
 * Created by Greg on 26/01/2018.
 */

interface SecurePreferences {
    fun setValue(key: String, value: String)
    fun getStringValue(key: String, defaultValue: String?): String?
}