package com.glureau.wolfram30

import com.glureau.wolfram30.encryption.BitsContainer
import com.glureau.wolfram30.encryption.WolframAutomataRule30Encryption2
import com.glureau.wolfram30.storage.RamSecurePreferences
import io.reactivex.Flowable
import org.junit.Test

/**
 * Created by Greg on 26/01/2018.
 */
class WolframAutomataRule30Encryption2Test {
    @Test
    fun testRule30() {
        val encryption = WolframAutomataRule30Encryption2(RamSecurePreferences())
        val privateKeyLength = WolframAutomataRule30Encryption2.KEY_SIZE
        val privateKey = BitsContainer(WolframAutomataRule30Encryption2.WORD_COUNT)
        val wordIndex= (privateKeyLength / BitsContainer.BITS_PER_WORD) / 2
        privateKey.words[wordIndex] = 0x10
        println(privateKey)
        WolframAutomataRule30Encryption2.PRIVATE_KEY = privateKey
        encryption.encrypt("", Flowable.fromArray(
                "aaaaa".toByteArray()))
                .blockingFirst()
    }
}