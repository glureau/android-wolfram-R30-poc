package com.glureau.wolfram30

import com.glureau.wolfram30.encryption.OBitSet
import com.glureau.wolfram30.encryption.Utils
import com.glureau.wolfram30.encryption.WolframAutomataRule30Encryption
import com.glureau.wolfram30.encryption.toBinaryString
import com.glureau.wolfram30.rx.FlowableUtils
import com.glureau.wolfram30.storage.RamSecurePreferences
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayOutputStream

/**
 * Created by Greg on 26/01/2018.
 */
class WolframAutomataRule30EncryptionTest {
    @Test
    fun testRule30() {
        val encryption = WolframAutomataRule30Encryption(RamSecurePreferences())

        // WARNING : This should be avoided! Potential side-effect on future tests
        WolframAutomataRule30Encryption.KEY_SIZE = 1
        val privateKeyLength = WolframAutomataRule30Encryption.KEY_SIZE
        val privateKey = OBitSet(privateKeyLength)
        privateKey.set(0, privateKeyLength, true)
        privateKey.flip(0)

        val messageLength = 100

        val keyStr = privateKey.toBinaryString()
//        println(keyStr.padEnd(privateKeyLength, '0')
//                .padStart(messageLength + privateKeyLength, '.')
//                .padEnd(messageLength + privateKeyLength + messageLength, '.'))

        val fullKey = encryption.generateEncryptionKey(privateKey, messageLength, null)
//        println(fullKey.toBinaryString())

        Assert.assertEquals("0100011001110100110110001010001100010101001111001101010010101000000111100001110101000111110110100111", fullKey.toBinaryString())
    }

    @Test
    fun testComplexScenario() {
        val originalMessage = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer pretium, arcu ut bibendum facilisis, ex sapien posuere quam, ut cursus eros lectus sit amet orci. Aliquam malesuada eleifend viverra. Donec nec lorem libero. Aenean id arcu turpis. Vivamus diam ex, tristique non commodo vel, venenatis sed metus. Sed sit amet luctus ante. Proin et facilisis sapien. Morbi hendrerit augue arcu, id placerat nisi auctor sit amet. Nullam sed augue accumsan, vestibulum velit nec, laoreet ligula cras amet."
        val inputStream = Utils.stringToInputStream(originalMessage)

        val encryption = WolframAutomataRule30Encryption(RamSecurePreferences())
        val privateKeyId = "complex_scenario"
        encryption.generateInitialKey(privateKeyId)

        val encryptedArrays = encryption.encrypt(privateKeyId, FlowableUtils.generate(inputStream)).toList().blockingGet()
        val encryptedStream = ByteArrayOutputStream()
        encryptedArrays.forEach { encryptedStream.write(it) }

        val decryptedArrays = encryption.decrypt(privateKeyId, FlowableUtils.generate(encryptedStream.toByteArray().inputStream())).toList().blockingGet()
        val decryptedStream = ByteArrayOutputStream()
        decryptedArrays.forEach { decryptedStream.write(it) }
        println("Assert that ${originalMessage}")
        println("is equal to ${String(decryptedStream.toByteArray())}")
        Assert.assertEquals(originalMessage, String(decryptedStream.toByteArray()))
    }
}