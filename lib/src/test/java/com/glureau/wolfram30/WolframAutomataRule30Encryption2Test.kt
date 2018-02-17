package com.glureau.wolfram30

import com.glureau.wolfram30.encryption.BitsContainer
import com.glureau.wolfram30.encryption.WolframAutomataRule30Encryption2
import com.glureau.wolfram30.encryption.toBinaryString
import com.glureau.wolfram30.storage.RamSecurePreferences
import io.reactivex.Flowable
import org.junit.Assert
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
        val wordIndex = (privateKeyLength / BitsContainer.BITS_PER_WORD) / 2
        privateKey.words[wordIndex] = BitsContainer.WORD_LAST_BIT
        println(privateKey)
        WolframAutomataRule30Encryption2.PRIVATE_KEY = privateKey
        val data = ByteArray(10, {0})
        val encrypted = encryption.encrypt("", Flowable.fromArray(data)).blockingFirst()
        Assert.assertEquals("10111001100010110010011101011100111010101100001100101011010101111110000111100010",
                encrypted.toBinaryString())
    }
}