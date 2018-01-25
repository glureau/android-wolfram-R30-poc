package com.glureau.wolfram30

import unsigned.Ubyte
import java.util.*

/**
 * Created by Greg on 25/01/2018.
 */
fun Ubyte.toBinaryString() = this.toInt().toString(2)

fun BitSet.toBinaryString(bufferSize: Int): String {
    val result = StringBuffer(bufferSize)
    for (i in 0..bufferSize) {
        result.append(if (this[i]) '1' else '0')
    }
    return result.toString()
}

fun BitSet.toBase64() = Base64.encode(this)
