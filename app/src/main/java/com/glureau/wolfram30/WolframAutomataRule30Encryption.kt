package com.glureau.wolfram30

import android.support.annotation.VisibleForTesting
import android.util.Log
import com.glureau.wolfram30.storage.SecurePreferences
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.schedulers.Schedulers
import java.util.*


/**
 * Created by Greg on 25/01/2018.
 */
class WolframAutomataRule30Encryption(val prefs: SecurePreferences) : Encryption {
    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        var KEY_SIZE = 1024 // bits
        val WORKSPACE_MAXIMUM_WIDTH =  4096 // (bits) Don't compute more than that width
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

    override fun encrypt(privateKeyId: String, data: OBitSet, result: OBitSet): Observable<Float> {
        return Observable.create<Float> { emitter ->
            val startTime = System.currentTimeMillis()


            val b64 = prefs.getStringValue(privateKeyId, null) ?: error("Cannot encrypt a message without private key")
            val privateKey = Base64.decode(b64)

            val generatedKey = generateEncryptionKey(privateKey, data.bitCount(), emitter)

            result.set(0, data.bitCount(), true)
            result.and(data)
            result.xor(generatedKey)
            emitter.onComplete()


            val duration = System.currentTimeMillis() - startTime
            Log.e("PERFORMANCE", "Encrypt duration: $duration ms")
            // v0: 200 chars = 400ms per encrypt (Pixel 2 emulated) [398-431] (skip 2st computes, JVM not ready yet)
            // v1: 200 chars = 270ms [253-305] (keep prev/current boolean instead of re-reading)
            // v1: 500 chars = 1150ms [1150-1221] (using more chars as durations looks more stable, and improvement should be more visible)
            // v2: 500 chars = 850ms [750-877] (fill the buffer of 1 before computing the new line, and set in the BitSet only when it's 0)
            // v3: 500 chars = 450ms [436-505] (OBitSet, no check, static size)
            // v4: 500 chars = 430ms [424-159] (Add tests and fix some computational issues

        }.subscribeOn(Schedulers.computation())
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun generateEncryptionKey(privateKey: OBitSet, keyLength: Int, progression: ObservableEmitter<Float>?): OBitSet {
        val triangleWidth = minOf(KEY_SIZE + keyLength * 2, WORKSPACE_MAXIMUM_WIDTH)
        val paddingLeft = (triangleWidth - KEY_SIZE) / 2
        val fullKeyColumn = (triangleWidth / 2)

        // Prepare the memory for computation
        val bufferA = OBitSet(triangleWidth)
        val bufferB = OBitSet(triangleWidth)

        // Initialize the first line
        bufferA.set(0, triangleWidth, true)
        for (i in 0 until KEY_SIZE) {
            bufferA[paddingLeft + i] = privateKey[i]
        }

        bufferB.set(0, triangleWidth, true)
        val fullKey = OBitSet(keyLength)
        for (i in 0 until keyLength) {
//            progression.onNext((i.toFloat() * 100f) / keyLength.toFloat())
            if (i % 2 == 0) {
                computeRule30Bool(bufferA, bufferB, triangleWidth)
//                println(bufferB.toBinaryString())
                fullKey[i] = bufferB[fullKeyColumn]
            } else {
                computeRule30Bool(bufferB, bufferA, triangleWidth)
//                println(bufferA.toBinaryString())
                fullKey[i] = bufferA[fullKeyColumn]
            }
        }
        return fullKey
    }


    private inline fun computeRule30Bool(input: OBitSet, output: OBitSet, bufferSize: Int) {
        var prev = input[0]
        var current = input[1]
        var next: Boolean
        // Set every bits to 1 before to only change 0s greatly improves write performance.
        output.set(0, output.bitCount() - 1, true)
        for (i in 1 until bufferSize - 1) {
            next = input[i + 1]
            if (!rule30(prev, current, next)) {
                output.flip(i)
            }
            prev = current
            current = next
        }
    }

    private inline fun rule30(a: Boolean, b: Boolean, c: Boolean): Boolean {
        if (!a && (b xor c)) return true
        if ((a == b) && (a == c)) return true
        return false
    }

    override fun decrypt(privateKeyId: String, data: OBitSet, result: OBitSet): Observable<Float> {
        // As it's the same principles : generating the pseudo-random key and XOR with original message, the decrypt is exactly the same operation.
        return encrypt(privateKeyId, data, result)
    }
}