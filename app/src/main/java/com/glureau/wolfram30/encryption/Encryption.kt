package com.glureau.wolfram30.encryption

import io.reactivex.Flowable
import io.reactivex.Observable

/**
 * Created by Greg on 25/01/2018.
 */
interface Encryption {
    fun generateInitialKey(privateKeyId: String): OBitSet

    // TODO : Replace with the magic (compose?) method to be aligned with RX philosophy
    fun encrypt(privateKeyId: String, input: Flowable<ByteArray>): Flowable<ByteArray>
}
