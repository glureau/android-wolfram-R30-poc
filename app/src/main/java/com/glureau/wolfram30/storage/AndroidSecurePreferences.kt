package com.glureau.wolfram30.storage

import de.adorsys.android.securestoragelibrary.SecurePreferences

class AndroidSecurePreferences : com.glureau.wolfram30.storage.SecurePreferences {
    override fun setValue(key: String, value: String) {
        SecurePreferences.setValue(key, value)
    }

    override fun getStringValue(key: String, defaultValue: String?) = SecurePreferences.getStringValue(key, defaultValue)
}