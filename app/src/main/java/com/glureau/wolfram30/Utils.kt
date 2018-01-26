package com.glureau.wolfram30

fun OBitSet.toBinaryString(): String {
    val result = StringBuffer(bitCount())
    for (i in 0 until bitCount()) {
        result.append(if (this[i]) '1' else '0')
    }
    return result.toString()
}

fun OBitSet.toBase64() = Base64.encode(this)
