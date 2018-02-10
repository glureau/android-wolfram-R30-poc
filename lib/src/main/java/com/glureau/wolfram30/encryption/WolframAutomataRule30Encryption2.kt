package com.glureau.wolfram30.encryption

import android.support.annotation.VisibleForTesting
import com.glureau.wolfram30.storage.SecurePreferences
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.experimental.xor


/**
 * Created by Greg on 25/01/2018.
 * Just trying some low-level implementation trying to improve perfs without starting native C/C++ dev.
 */
class WolframAutomataRule30Encryption2(val prefs: SecurePreferences) {
    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        var KEY_SIZE = 1024 // bits (Actually no good reason to be different than workspace max width...)
        var KEY_BIT_POSITION = KEY_SIZE / 2
        var WORD_COUNT = KEY_SIZE / BitsContainer.BITS_PER_WORD
        private val WORKSPACE_MAXIMUM_WIDTH = KEY_SIZE // (bits) Don't compute more than that width

        var PRIVATE_KEY = BitsContainer(0) // TODO : Save it on disk

        private val PREVIOUS = BitsContainer(WORD_COUNT)
        private val LEFT = BitsContainer(WORD_COUNT)
        private val RIGHT = BitsContainer(WORD_COUNT)
        private val CURRENT = BitsContainer(WORD_COUNT)
        private val TMP1 = BitsContainer(WORD_COUNT)
        private val TMP2 = BitsContainer(WORD_COUNT)
        private val TMP3 = BitsContainer(WORD_COUNT)
        private var RESULT: Byte = 0
    }

    fun generateInitialKey(privateKeyId: String): BitsContainer {
        val privateKey = BitsContainer(WORD_COUNT)
//        val rand = SecureRandom()
        val rand = Random(0) // TODO: Use SecureRandom indeed.
        for (i in 0 until WORD_COUNT) {
            privateKey.words[i] = rand.nextLong()
        }
        storePrivateKey(privateKeyId, privateKey)
        return privateKey
    }

    fun setEncryptionKey(privateKeyId: String, privateKey: BitsContainer) {
        storePrivateKey(privateKeyId, privateKey)
    }

    private fun storePrivateKey(privateKeyId: String, privateKey: BitsContainer) {
        PRIVATE_KEY = privateKey
        //prefs.setValue(privateKeyId, privateKey.toBase64())
    }

    fun decrypt(privateKeyId: String, input: Flowable<ByteArray>): Flowable<ByteArray> {
        return encrypt(privateKeyId, input)
    }

    fun encrypt(privateKeyId: String, input: Flowable<ByteArray>): Flowable<ByteArray> {
        //val b64 = prefs.getStringValue(privateKeyId, null)
        //        ?: error("Cannot encrypt a message without private key")
        //val privateKey = Base64.decode(b64)
        CURRENT.copyFrom(PRIVATE_KEY)
        return input.map { ba ->
            //val requiredWordCount = (ba.size.toDouble() / 8.0).nextUp().toInt()
            val resultByteArray = ByteArray(ba.size)
            for (byteIndex in 0 until ba.size) {
                RESULT = 0
                for (bitIndexInByte in 0 until 8) {
                    // Prepare buffers
                    CURRENT.copyTo(PREVIOUS)
                    CURRENT.copyTo(LEFT)
                    CURRENT.copyTo(RIGHT)
                    LEFT.left()
                    RIGHT.right()
                    /**
                     * Rule 30 can be described as:
                     * "look at each cell and its right hand
                     * neighbor. If both of these were white
                     * on the previous step, then take the color
                     * of the cell to be whatever the previous
                     * color of its left-hand neighbor was.
                     * Otherwise, take the new color to be opposite
                     * of the left hand neighbor."
                     *
                     * Given the names L/C/R (left/center/right)
                     * it can be translated in 2 rules:
                     * C == R ? => L
                     * C != R => !L
                     *
                     * != <=> XOR, so it can be written as :
                     * !LEFT AND (CURRENT XOR RIGHT) OR
                     * LEFT AND !(CURRENT XOR RIGHT)
                     */
                    PREVIOUS.xor(RIGHT, TMP1) // TMP1= (C != R)
                    LEFT.inv(TMP3)// TMP3 = !L
                    TMP3.and(TMP1, TMP2)// TMP2 = !L & (C!=R)
                    TMP1.inv() // TMP1= (C == R)
                    LEFT.and(TMP1, TMP3) // TMP3 = L & (C==R)
                    TMP2.or(TMP3, CURRENT)
                    println(CURRENT)
                    val keyBit = CURRENT.getBitAt(KEY_BIT_POSITION)
                    if (keyBit) {
                        RESULT.plus(1 shl bitIndexInByte)
                    }
                }
                resultByteArray[byteIndex] = ba[byteIndex] xor RESULT
            }
            //storePrivateKey(privateKeyId, CURRENT)
            resultByteArray
        }.subscribeOn(Schedulers.computation())
    }
}