package com.glureau.wolfram30.encryption

import java.io.ByteArrayInputStream

fun OBitSet.toBinaryString(): String {
    val result = StringBuffer(bitCount())
    for (i in 0 until bitCount()) {
        result.append(if (this[i]) '1' else '0')
    }
    return result.toString()
}

fun OBitSet.toBase64() = Base64.encode(this)

object Utils {
    fun stringToInputStream(data: String) = ByteArrayInputStream(data.toByteArray())
}