package com.glureau.wolfram30

import io.reactivex.Observable

/**
 * Created by Greg on 25/01/2018.
 */
interface Encryption {
    fun generateInitialKey(privateKeyId: String): OBitSet
    fun setEncryptionKey(privateKeyId: String, privateKey: OBitSet)

    fun encrypt(privateKeyId: String, data: OBitSet, result: OBitSet): Observable<Float>
    fun decrypt(privateKeyId: String, data: OBitSet, result: OBitSet): Observable<Float>
}
