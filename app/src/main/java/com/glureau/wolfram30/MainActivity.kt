package com.glureau.wolfram30

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import unsigned.Ubyte
import unsigned.toUbyte

class MainActivity : AppCompatActivity() {

    companion object {
        val BYTE_FF: Ubyte = Ubyte(0xFF) // 11111111
        val BYTE_FE: Ubyte = Ubyte(0xFE) // 11111110
        val BYTE_7F: Ubyte = Ubyte(0x7F) // 01111111
        val BYTE_01: Ubyte = Ubyte(0x01)// 00000001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val message = "abcdef".toByteArray()
        val privateKey = "12345".toByteArray()
        Log.e("OOO", "message=${message.contentToString()}")
        Log.e("OOO", "privateKey=${privateKey.contentToString()}")

        val fullKeyLength = message.size * 8
        val triangleWidth = privateKey.size * 8 + fullKeyLength * 2
        Log.e("OOO", "fullKeyLength=$fullKeyLength")
        Log.e("OOO", "triangleWidth=$triangleWidth")

        // Prepare the space to compute
//        val table = mutableListOf<ByteArray>()
        val table = mutableListOf<BooleanArray>()

        // Initialize the first line
//        val firstLine = ByteArray(triangleWidth, { BYTE_FF.toByte() })
        val firstLine = BooleanArray(triangleWidth, { true })
        privateKey.forEachIndexed(action = { i, b ->
            //            firstLine[fullKeyLength + i] = b
            val ub = b.toUbyte()
            Log.e("OOO", "Fill from ${(message.size + i) * 8 + 0} to ${(message.size + i) * 8 + 7}")
            firstLine[(message.size + i) * 8 + 0] = ((ub shr 7) rem 2) == BYTE_01
            firstLine[(message.size + i) * 8 + 1] = ((ub shr 6) rem 2) == BYTE_01
            firstLine[(message.size + i) * 8 + 2] = ((ub shr 5) rem 2) == BYTE_01
            firstLine[(message.size + i) * 8 + 3] = ((ub shr 4) rem 2) == BYTE_01
            firstLine[(message.size + i) * 8 + 4] = ((ub shr 3) rem 2) == BYTE_01
            firstLine[(message.size + i) * 8 + 5] = ((ub shr 2) rem 2) == BYTE_01
            firstLine[(message.size + i) * 8 + 6] = ((ub shr 1) rem 2) == BYTE_01
            firstLine[(message.size + i) * 8 + 7] = ((ub shr 0) rem 2) == BYTE_01
        })
        table.add(firstLine)

        Log.e("OOO", "BYTE_01=" + BYTE_01.toBinaryString())
        Log.e("OOO", "BYTE_FF=" + BYTE_FF.toBinaryString())
        Log.e("OOO", "BYTE_7F=" + BYTE_7F.toBinaryString())
        Log.e("OOO", "BYTE_FE=" + BYTE_FE.toBinaryString())

        Log.e("000", "privateKey=${privateKey.contentToString()}")
        Log.e("000", "binarized=" + firstLine.joinToString(separator = "", transform = { b -> if (b) "1" else "0" }))

        for (i in 1 until fullKeyLength) {
            table.add(computeRule30Bool(table[i - 1]))
            Log.e("000", "binarized=" + table[i].joinToString(separator = "", transform = { b -> if (b) "1" else "0" }))
        }

        text.setText("hello")
    }

    fun computeRule30Bool(input: BooleanArray): BooleanArray {
        val result = BooleanArray(input.size, { true })
        for (i in 1 until input.size - 1) {
            val prev = input[i - 1]
            val current = input[i]
            val next = input[i + 1]
            result[i] = rule30(prev, current, next)
        }
        return result
    }

    fun rule30(a: Boolean, b: Boolean, c: Boolean): Boolean {
        //return (a == b == c) || (!a && (b xor c))
        if (!a && !b && !c) return true
        if (!a && !b && c) return true
        if (!a && b && !c) return true
        if (a && b && c) return true
        return false
    }

    fun computeRule30_optimized(bytes: ByteArray): ByteArray {
        val result = ByteArray(bytes.size)
        bytes.forEachIndexed({ byteIndex, byte ->
            result[byteIndex] = computeRule30_optimized(
                    if (byteIndex > 0) bytes[byteIndex - 1] else null,
                    byte,
                    if (byteIndex < bytes.size) bytes[byteIndex + 1] else null)
        })
        return result
    }

    /**
     * Compute the Wolfram automata rule 30
     *
     */
    fun computeRule30_optimized(before: Byte?, current: Byte, after: Byte?): Byte {
        val result: Byte = 0

        var prevBit = if (before != null) ((before.toUbyte() or BYTE_FE) and BYTE_FF) == BYTE_FF else true
        var nextBit = if (after != null) ((after.toUbyte() or BYTE_7F) and BYTE_FF) == BYTE_FF else true
        TODO("Implement with bytes to reduce memory usage & speed computes")

        return result
    }
}
