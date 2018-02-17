package com.glureau.wolfram30.encryption

import android.util.Base64
import android.util.Base64.DEFAULT

/**
 * Similar to BitSet but no check, and managed as a cyclic buffer (left/right operations)
 */
class BitsContainer(val wordCount: Int) {
    val words = LongArray(wordCount)

    companion object {
        /**
         * Currently a word is a long, which consists of 64 bits, requiring 6 address bits.
         * The choice of word size is determined purely by performance concerns.
         */
        const val ADDRESS_BITS_PER_WORD = 6
        const val BITS_PER_WORD = 1 shl ADDRESS_BITS_PER_WORD

        const val WORD_MASK = -0x1L
        const val WORD_FIRST_BIT_MASK = WORD_MASK shl 1
        const val WORD_FIRST_BIT = WORD_MASK xor WORD_FIRST_BIT_MASK
        const val WORD_LAST_BIT_MASK = WORD_MASK ushr 1
        const val WORD_LAST_BIT = WORD_MASK xor WORD_LAST_BIT_MASK

        const val TO_STRING_WORD_SEPARATOR = ""

        fun wordIndex(bitPos: Int) = bitPos shr ADDRESS_BITS_PER_WORD
        fun bitIndex(wordIndex: Int) = 1L shl wordIndex(wordIndex)
    }

    fun copyTo(target: BitsContainer) {
        for (i in 0 until wordCount) {
            target.words[i] = words[i]
        }
    }

    /**
     * Move all bits from one position to the left.
     * First bit becomes the last one.
     */
    fun left() {
        val firstRemainingBit = words[0] < 0L
        for (i in 0 until wordCount) {
            val remainingBit: Boolean
            if (i < wordCount - 1) {
                remainingBit = words[i + 1] < 0L
            } else {
                remainingBit = firstRemainingBit // Cyclic buffer : the first remaining is added at the end.
            }
            words[i] = words[i] shl 1
            if (remainingBit) {
                words[i] += 1L
            }
        }
    }

    /**
     * Move all bits from one position to the left.
     * First bit becomes the last one.
     */
    fun right() {
        val lastRemainingBit = words[wordCount - 1] % 2 != 0L
        for (i in wordCount - 1 downTo 0) {
            val remainingBit: Boolean
            if (i >= 1) {
                remainingBit = words[i - 1] % 2 != 0L
            } else {
                remainingBit = lastRemainingBit // Cyclic buffer : the last remaining is added on the first bit.
            }
            val sign = words[i] >= 0
            words[i] = words[i] shr 1
            if (sign == remainingBit) {
                words[i] += 1L shl 63 // flip first bit
            }
        }
    }

    fun and(other: BitsContainer, output: BitsContainer) {
        for (i in 0 until wordCount) {
            output.words[i] = words[i] and other.words[i]
        }
    }

    fun or(other: BitsContainer, output: BitsContainer) {
        for (i in 0 until wordCount) {
            output.words[i] = words[i] or other.words[i]
        }
    }

    fun xor(other: BitsContainer, output: BitsContainer) {
        for (i in 0 until wordCount) {
            output.words[i] = words[i] xor other.words[i]
        }
    }

    fun inv(output: BitsContainer) {
        for (i in 0 until wordCount) {
            output.words[i] = words[i].inv()
        }
    }

    fun inv() {
        for (i in 0 until wordCount) {
            words[i] = words[i].inv()
        }
    }

    override fun toString() = toBinaryString()

    fun toBinaryString(): String {
        val sb = StringBuffer()
        words.forEach {
            sb.append(it.toBinaryString())
            sb.append(TO_STRING_WORD_SEPARATOR)
        }
        return sb.toString()
    }

    fun copyFrom(source: BitsContainer) {
        source.words.forEachIndexed({ index, word ->
            words[index] = word
        })
    }


    fun getBitAt(bitPos: Int): Boolean {
        val wordIndex = wordIndex(bitPos)
        return (words[wordIndex] and (1L shl (64 - bitPos.rem(63)))) != 0L
    }

    fun bitCount() = wordCount * BITS_PER_WORD
    fun toBase64(): String {
        return Base64.encodeToString(toByteArray(), DEFAULT)
    }

    private fun toByteArray(): ByteArray {
        val result = ByteArray(bitCount() / 8)
        words.forEachIndexed({ i, word ->
            val index = i * 8
            result[index] = (word ushr 0).toByte()
            result[index + 1] = (word ushr 8).toByte()
            result[index + 2] = (word ushr 16).toByte()
            result[index + 3] = (word ushr 24).toByte()
            result[index + 4] = (word ushr 32).toByte()
            result[index + 5] = (word ushr 40).toByte()
            result[index + 6] = (word ushr 48).toByte()
            result[index + 7] = (word ushr 56).toByte()
        })
        return result
    }

    fun fromBase64(b64: String) {
        val buff = Base64.decode(b64, DEFAULT)
        words.forEachIndexed({ i, _ ->
            val index = i * 8
            words[i] = (buff[index + 0].toULong()) or
                    (buff[index + 1].toULong() shl 8) or
                    (buff[index + 2].toULong() shl 16) or
                    (buff[index + 3].toULong() shl 24) or
                    (buff[index + 4].toULong() shl 32) or
                    (buff[index + 5].toULong() shl 40) or
                    (buff[index + 6].toULong() shl 48) or
                    (buff[index + 7].toULong() shl 56)
        })
    }
}