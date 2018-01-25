package com.glureau.wolfram30

import de.adorsys.android.securestoragelibrary.SecurePreferences
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.schedulers.Schedulers
import java.security.SecureRandom
import java.util.*


/**
 * Created by Greg on 25/01/2018.
 */
class WolframAutomataRule30Encryption : Encryption {
    companion object {
        val KEY_SIZE = 1024 // bits
    }

    override fun generateInitialKey(privateKeyId: String): BitSet {
        val privateKey = BitSet(KEY_SIZE)
//        val rand = SecureRandom()
        val rand = Random(0)
        for (i in 0..KEY_SIZE) {
            privateKey[i] = rand.nextBoolean()
        }
        storePrivateKey(privateKeyId, privateKey)
        return privateKey
    }

    override fun setEncryptionKey(privateKeyId: String, privateKey: BitSet) {
        storePrivateKey(privateKeyId, privateKey)
    }

    private fun storePrivateKey(privateKeyId: String, privateKey: BitSet) {
        SecurePreferences.setValue(privateKeyId, privateKey.toBase64())
    }

    override fun encrypt(privateKeyId: String, data: BitSet, result: BitSet): Observable<Float> {
        return Observable.create<Float> { emitter ->
            val b64 = SecurePreferences.getStringValue(privateKeyId, null) ?: error("Cannot encrypt a message without private key")
            val privateKey = Base64.decode(b64)

            val generatedKey = generateEncryptionKey(privateKey, data.size(), emitter)

            result.set(0, data.size(), true)
            result.and(data)
            result.xor(generatedKey)
            emitter.onComplete()
        }.subscribeOn(Schedulers.computation())
    }

    private fun generateEncryptionKey(privateKey: BitSet, keyLength: Int, progression: ObservableEmitter<Float>): BitSet {
        val privateKeySize = privateKey.size()
        val triangleWidth = privateKeySize + keyLength * 2
        val fullKeyColumn = (triangleWidth / 2) + 1

        // Prepare the memory for computation
        val bufferA = BitSet(triangleWidth)
        val bufferB = BitSet(triangleWidth)

        // Initialize the first line
        bufferA.set(0, triangleWidth, true)
        for (i in 0..privateKeySize) {
            bufferA[keyLength + i] = privateKey[i]
        }

        val fullKey = BitSet(keyLength)
        for (i in 1 until keyLength) {
            progression.onNext((i.toFloat() * 100f) / keyLength.toFloat())
            if (i % 2 == 1) {
                computeRule30Bool(bufferA, bufferB, triangleWidth)
                fullKey[i] = bufferB[fullKeyColumn]
            } else {
                computeRule30Bool(bufferB, bufferA, triangleWidth)
                fullKey[i] = bufferA[fullKeyColumn]
            }
        }
        return fullKey
    }


    private fun computeRule30Bool(input: BitSet, output: BitSet, bufferSize: Int) {
        for (i in 1 until bufferSize - 1) {
            val prev = input[i - 1]
            val current = input[i]
            val next = input[i + 1]
            output[i] = rule30(prev, current, next)
        }
    }

    private fun rule30(a: Boolean, b: Boolean, c: Boolean): Boolean {
        //return (a == b == c) || (!a && (b xor c))
        if (!a && !b && !c) return true
        if (!a && !b && c) return true
        if (!a && b && !c) return true
        if (a && b && c) return true
        return false
    }

    override fun decrypt(privateKeyId: String, data: BitSet, result: BitSet): Observable<Float> {
        // As it's the same principles : generating the pseudo-random key and XOR with original message, the decrypt is exactly the same operation.
        return encrypt(privateKeyId, data, result)
    }
}