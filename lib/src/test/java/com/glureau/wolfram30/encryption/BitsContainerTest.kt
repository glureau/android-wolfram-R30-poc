package com.glureau.wolfram30.encryption

import junit.framework.Assert.assertEquals
import org.junit.Test
import java.util.*

/**
 * Created by greg on 08/02/2018.
 */
class BitsContainerTest {

    val wordSeparator = BitsContainer.TO_STRING_WORD_SEPARATOR

    @Test
    fun printlnAAAA() {
        val buffer = BitsContainer(10)
        var expected = ""
        for (i in 0 until 10) {
            buffer.words[i] = -0x5555555555555556
            expected += "1010101010101010101010101010101010101010101010101010101010101010" + wordSeparator
        }
        println(buffer)
        assertEquals(expected, buffer.toString())
    }

    @Test
    fun println5555() {
        val buffer = BitsContainer(10)
        var expected = ""
        for (i in 0 until 10) {
            buffer.words[i] = 0x5555555555555555
            expected += "0101010101010101010101010101010101010101010101010101010101010101" + wordSeparator
        }
        println(buffer)
        assertEquals(expected, buffer.toString())
    }


    @Test
    fun left() {
        val buffer = BitsContainer(3)
        val rand = Random(123)
        for (i in 0 until 3) {
            buffer.words[i] = rand.nextLong()
        }

        for (i in 0 until 10) {
            println(buffer)
            buffer.left()
        }
        println(buffer)
        val expected = "100001111100011101110100111100101111000000010010010101111111011010101110001100110100010100110100110011111100001010101001000000110101111101000100010101100100101111101000100110001100101011100100"
        assertEquals(expected, buffer.toString())
    }

    @Test
    fun rightRandom() {
        val buffer = BitsContainer(3)
        val rand = Random(123)
        for (i in 0 until 3) {
            buffer.words[i] = rand.nextLong()
        }

        val expected = "100011001010111001001000011111000111011101001111001011110000000100100101011111110110101011100011001101000101001101001100111111000010101010010000001101011111010001000101011001001011111010001001"
        testRight(buffer, 10, expected)
    }

    private fun testRight(buffer: BitsContainer, callsCount: Int, expected: String) {
        for (i in 0 until callsCount) {
            println(buffer)
            buffer.right()
        }
        println(buffer)
        assertEquals(expected, buffer.toString())
    }

    @Test
    fun right0000() {
        val buffer = BitsContainer(3)
        val expected = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
        testRight(buffer, 10, expected)
    }
}