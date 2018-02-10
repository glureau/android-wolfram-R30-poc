package com.glureau.wolfram30.encryption

import android.support.annotation.VisibleForTesting
import com.glureau.wolfram30.storage.SecurePreferences
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.experimental.xor


/**
 * Created by Greg on 25/01/2018.
 */
class WolframAutomataRule30Encryption(val prefs: SecurePreferences) : Encryption, Decryption {
    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        var KEY_SIZE = 1024 // bits (Actually no good reason to be different than workspace max width...)
        private val WORKSPACE_MAXIMUM_WIDTH = KEY_SIZE // (bits) Don't compute more than that width
        private val BITS_IN_BYTE = 8
    }

    override fun generateInitialKey(privateKeyId: String): OBitSet {
        val privateKey = OBitSet(KEY_SIZE)
//        val rand = SecureRandom()
        val rand = Random(0) // TODO: Use SecureRandom indeed.
        for (i in 0 until KEY_SIZE) {
            privateKey[i] = rand.nextBoolean()
        }
        storePrivateKey(privateKeyId, privateKey)
        return privateKey
    }

    override fun setEncryptionKey(privateKeyId: String, privateKey: OBitSet) {
        storePrivateKey(privateKeyId, privateKey)
    }

    private fun storePrivateKey(privateKeyId: String, privateKey: OBitSet) {
        prefs.setValue(privateKeyId, privateKey.toBase64())
    }
    override fun encrypt(privateKeyId: String, input: Flowable<ByteArray>): Flowable<ByteArray> {
        val b64 = prefs.getStringValue(privateKeyId, null) ?: error("Cannot encrypt a message without private key")
        val privateKey = Base64.decode(b64)
        return input.map { ba ->
            val (fullKeyOBitSet, lastLine) = generateEncryptionKey(privateKey, ba.size * BITS_IN_BYTE)
            val fullKey = fullKeyOBitSet.toByteArray()
            ba.forEachIndexed { index, byte ->
                fullKey[index] = byte xor fullKey[index]
            }
            storePrivateKey(privateKeyId, lastLine)
            fullKey
        }.subscribeOn(Schedulers.computation())
    }

    override fun decrypt(privateKeyId: String, input: Flowable<ByteArray>): Flowable<ByteArray> {
        return encrypt(privateKeyId, input)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun generateEncryptionKey(privateKey: OBitSet, keyLength: Int): Pair<OBitSet, OBitSet> {
        val triangleWidth = minOf(KEY_SIZE + keyLength * 2, WORKSPACE_MAXIMUM_WIDTH)
        val paddingLeft = (triangleWidth - KEY_SIZE) / 2
        val fullKeyColumn = (triangleWidth / 2)

        // Prepare the memory for computation
        var bufferInput = OBitSet(triangleWidth)
        var bufferOutput = OBitSet(triangleWidth)

        // Initialize the first line
        bufferInput.set(0, triangleWidth, true)
        for (i in 0 until KEY_SIZE) {
            bufferInput[paddingLeft + i] = privateKey[i]
        }

        bufferOutput.set(0, triangleWidth, true)
        val fullKey = OBitSet(keyLength)
        var bufferTemp: OBitSet
        for (i in 0 until keyLength) {

            computeRule30Bool(bufferInput, bufferOutput, triangleWidth)
            fullKey[i] = bufferOutput[fullKeyColumn]

            // Swap bufferInput and bufferOuput
            bufferTemp = bufferInput
            bufferInput = bufferOutput
            bufferOutput = bufferTemp
        }
        return Pair(fullKey, bufferInput)
    }


    private fun computeRule30Bool(input: OBitSet, output: OBitSet, bufferSize: Int) {
        // Set every bits to 1 before to only change 0s greatly improves write performance.
        output.set(0, output.bitCount() - 1, true)
        computeRule30Bool(input, output, bufferSize, 1, bufferSize - 1)
    }

    private fun computeRule30Bool(input: OBitSet, output: OBitSet, bufferSize: Int, start: Int, end: Int) {
        var prev = input[start - 1]
        var current = input[start]
        var next: Boolean
        for (i in start until end) {
            next = input[i + 1]
            if (!rule30(prev, current, next)) {
                output.flip(i)
            }
            prev = current
            current = next
        }
    }

    private fun rule30(a: Boolean, b: Boolean, c: Boolean): Boolean {
        if (!a && (b xor c)) return true
        if ((a == b) && (a == c)) return true
        return false
    }
}