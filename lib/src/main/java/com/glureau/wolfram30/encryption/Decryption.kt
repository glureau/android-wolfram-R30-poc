package com.glureau.wolfram30.encryption

import io.reactivex.Flowable

/**
 * Created by Greg on 25/01/2018.
 */
interface Decryption {
    fun setEncryptionKey(privateKeyId: String, privateKey: OBitSet)

    fun decrypt(privateKeyId: String, input: Flowable<ByteArray>): Flowable<ByteArray>
}