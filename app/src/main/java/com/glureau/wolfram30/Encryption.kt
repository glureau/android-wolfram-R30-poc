package com.glureau.wolfram30

import io.reactivex.Observable
import java.util.*

/**
 * Created by Greg on 25/01/2018.
 */
interface Encryption {
    fun generateInitialKey(privateKeyId: String): BitSet
    fun setEncryptionKey(privateKeyId: String, privateKey: BitSet)

    fun encrypt(privateKeyId: String, data: BitSet, result: BitSet): Observable<Float>
    fun decrypt(privateKeyId: String, data: BitSet, result: BitSet): Observable<Float>
}
