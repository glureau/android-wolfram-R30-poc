package com.glureau.wolfram30.storage

class RamSecurePreferences : SecurePreferences {
    val map = mutableMapOf<String, String>()
    override fun setValue(key: String, value: String) {
        map.put(key, value)
    }

    override fun getStringValue(key: String, defaultValue: String?) = map[key] ?: defaultValue
}