package com.glureau.wolfram30.encryption

import android.support.annotation.VisibleForTesting
import com.glureau.wolfram30.encryption.BitsContainer.Companion.BITS_PER_WORD
import com.glureau.wolfram30.storage.SecurePreferences
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.experimental.xor


/**
 * Created by Greg on 25/01/2018.
 * Just trying some low-level implementation trying to improve perfs without starting native C/C++ dev.
 */
class WolframAutomataRule30Encryption(val prefs: SecurePreferences) : Encryption, Decryption {
    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        var KEY_SIZE = 1024 // bits (Actually no good reason to be different than workspace max width...)
        var KEY_BIT_POSITION = KEY_SIZE / 2
        var KEY_BIT_WORD_POSITION = BitsContainer.wordIndex(KEY_BIT_POSITION)
        var KEY_BIT_MASK = (1L shl (BITS_PER_WORD - KEY_BIT_POSITION.rem(BITS_PER_WORD - 1)))
        var WORD_COUNT = KEY_SIZE / BitsContainer.BITS_PER_WORD

        //var PRIVATE_KEY = BitsContainer(0) // TODO : Save it on disk

        private val PREVIOUS = BitsContainer(WORD_COUNT)
        private val LEFT = BitsContainer(WORD_COUNT)
        private val RIGHT = BitsContainer(WORD_COUNT)
        private val CURRENT = BitsContainer(WORD_COUNT)
        private val TMP1 = BitsContainer(WORD_COUNT)
        private val TMP2 = BitsContainer(WORD_COUNT)
        private val TMP3 = BitsContainer(WORD_COUNT)
        private var RESULT: Byte = 0
    }

    override fun generateInitialKey(privateKeyId: String): BitsContainer {
        val privateKey = BitsContainer(WORD_COUNT)
//        val rand = SecureRandom()
        val rand = Random(0) // TODO: Use SecureRandom indeed.
        for (i in 0 until WORD_COUNT) {
            privateKey.words[i] = rand.nextLong()
        }
        storePrivateKey(privateKeyId, privateKey)
        return privateKey
    }

    override fun setEncryptionKey(privateKeyId: String, privateKey: BitsContainer) {
        storePrivateKey(privateKeyId, privateKey)
    }

    private fun storePrivateKey(privateKeyId: String, privateKey: BitsContainer) {
        prefs.setValue(privateKeyId, privateKey.toBase64())
    }

    override fun decrypt(privateKeyId: String, input: Flowable<ByteArray>): Flowable<ByteArray> {
        return encrypt(privateKeyId, input)
    }

    override fun encrypt(privateKeyId: String, input: Flowable<ByteArray>): Flowable<ByteArray> {
        val b64 = prefs.getStringValue(privateKeyId, null)
                ?: error("Cannot encrypt a message without private key")
        CURRENT.fromBase64(b64)
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
                    // As we want the same column to represent left/right, the copy moved to the right is LEFT (and vice-versa)
                    LEFT.right()
                    RIGHT.left()
                    /**
                     * Rule 30 can be described as:
                     * "look at each cell and its right hand
                     * neighbor. If both of these were white [0]
                     * on the previous step, then take the color
                     * of the cell to be whatever the previous
                     * color of its left-hand neighbor was.
                     * Otherwise, take the new color to be opposite
                     * of the left hand neighbor."
                     *
                     * Given the names L/C/R (left/center/right)
                     * it can be translated in 2 rules:
                     * C == R == 0 => L
                     * C != R => !L
                     *
                     * != <=> XOR, so it can be written as :
                     * LEFT AND !(CURRENT AND RIGHT)
                     *    OR
                     * !LEFT AND (CURRENT OR RIGHT)
                     */

                    PREVIOUS.inv()
                    RIGHT.inv()
                    PREVIOUS.and(RIGHT, TMP1) // TMP1= (!C & !R)
                    TMP1.and(LEFT, TMP2) // TMP2= !L & (!C&!R)

                    TMP1.inv()
                    LEFT.inv()
                    TMP1.and(LEFT, TMP3)
                    TMP2.or(TMP3, CURRENT)

                    //println(CURRENT)
                    val keyBit = CURRENT.words[KEY_BIT_WORD_POSITION] and KEY_BIT_MASK != 0L
                    //println("keyBit= $keyBit")
                    if (keyBit) {
                        RESULT = RESULT.plus((1 shl (7 - bitIndexInByte))).toByte()
                        //println("RESULT= ${RESULT.toBinaryString()}")
                    }
                }
                resultByteArray[byteIndex] = ba[byteIndex] xor RESULT
                //println("ba[byteIndex] (${ba[byteIndex].toBinaryString()}) xor RESULT (${RESULT.toBinaryString()}) = " + (ba[byteIndex] xor RESULT).toBinaryString())
            }
            storePrivateKey(privateKeyId, CURRENT)
            resultByteArray
        }.subscribeOn(Schedulers.computation())
    }
}