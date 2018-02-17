package com.glureau.wolfram30

import com.glureau.wolfram30.encryption.BitsContainer
import com.glureau.wolfram30.encryption.WolframAutomataRule30Encryption
import com.glureau.wolfram30.encryption.toBinaryString
import com.glureau.wolfram30.storage.RamSecurePreferences
import io.reactivex.Flowable
import org.junit.Assert
import org.junit.Test

/**
 * Created by Greg on 26/01/2018.
 */
class WolframAutomataRule30EncryptionTest {
    @Test
    fun testRule30() {
        val encryption = WolframAutomataRule30Encryption(RamSecurePreferences())
        val privateKeyLength = WolframAutomataRule30Encryption.KEY_SIZE
        val privateKey = BitsContainer(WolframAutomataRule30Encryption.WORD_COUNT)
        val wordIndex = (privateKeyLength / BitsContainer.BITS_PER_WORD) / 2
        privateKey.words[wordIndex] = BitsContainer.WORD_LAST_BIT
        println(privateKey)
        encryption.setEncryptionKey("toto", privateKey)
        val data = ByteArray(10, { 0 })
        val encrypted = encryption.encrypt("toto", Flowable.fromArray(data)).blockingFirst()
        Assert.assertEquals("10111001100010110010011101011100111010101100001100101011010101111110000111100010",
                encrypted.toBinaryString())
    }

    @Test
    fun testPerformance() {
        val encryption = WolframAutomataRule30Encryption(RamSecurePreferences())
        encryption.generateInitialKey("titi")
        val startTime = System.currentTimeMillis()
        var result: ByteArray? = null
        for (i in 0 until 10* 1024) {
            val data = ByteArray(1024, { 0 })
            result = encryption.encrypt("titi", Flowable.fromArray(data)).blockingFirst()
        }
        println("last result: ${result?.toBinaryString()}")
        println("Performance test : " + (System.currentTimeMillis() - startTime) + "ms")
    }
}