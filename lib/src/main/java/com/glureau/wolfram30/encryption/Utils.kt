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

fun String.toInputStream() = ByteArrayInputStream(toByteArray())


fun ByteArray.toBinaryString(): String {
    val sb = StringBuffer()
    iterator().forEach {
        val tmp = StringBuffer()
        val integer = it.toInt()
        for (i in 0..7) {
            if ((integer shr (7 - i)) % 2 != 0) {
                tmp.append('1')
            } else {
                tmp.append('0')
            }
        }
        sb.append(tmp)
        sb.append(BitsContainer.TO_STRING_WORD_SEPARATOR)
    }
    return sb.toString()
}

fun Long.toBinaryString(): StringBuffer {
    val tmp = StringBuffer()
    for (i in 0..63) {
        if ((this shr (63 - i)) % 2 != 0L) {
            tmp.append('1')
        } else {
            tmp.append('0')
        }
    }
    return tmp
}

fun Byte.toBinaryString(): StringBuffer {
    val tmp = StringBuffer()
    for (i in 0..7) {
        if ((this.toLong() shr (7 - i)) % 2 != 0L) {
            tmp.append('1')
        } else {
            tmp.append('0')
        }
    }
    return tmp
}
