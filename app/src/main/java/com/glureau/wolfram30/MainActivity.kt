package com.glureau.wolfram30

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import unsigned.Ubyte
import unsigned.toUbyte
import java.util.*

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

//        val message = "abcdef".toByteArray()
        //val privateKey = "12345".toByteArray()

        // Random data
        val message = kotlin.ByteArray(4096)
        Random(1).nextBytes(message)
        // Crypto-secure
        val privateKey = kotlin.ByteArray(1024/8)
        Random(1).nextBytes(privateKey)

        val fullKeyLength = message.size * 8
        val triangleWidth = privateKey.size * 8 + fullKeyLength * 2
        val fullKeyColumn = (triangleWidth / 2) + 1

        // Initialize the first line
        val bufferA = BooleanArray(triangleWidth, { true })
        val bufferB = BooleanArray(triangleWidth, { true })
        privateKey.forEachIndexed(action = { i, b ->
            val ub = b.toUbyte()
            val lineOffset = message.size + i
            bufferA[lineOffset * 8 + 0] = ((ub shr 7) rem 2) == BYTE_01
            bufferA[lineOffset * 8 + 1] = ((ub shr 6) rem 2) == BYTE_01
            bufferA[lineOffset * 8 + 2] = ((ub shr 5) rem 2) == BYTE_01
            bufferA[lineOffset * 8 + 3] = ((ub shr 4) rem 2) == BYTE_01
            bufferA[lineOffset * 8 + 4] = ((ub shr 3) rem 2) == BYTE_01
            bufferA[lineOffset * 8 + 5] = ((ub shr 2) rem 2) == BYTE_01
            bufferA[lineOffset * 8 + 6] = ((ub shr 1) rem 2) == BYTE_01
            bufferA[lineOffset * 8 + 7] = ((ub shr 0) rem 2) == BYTE_01
        })

        Log.e("000", "binarized=" + bufferA.joinToString(separator = "", transform = { b -> if (b) "1" else "0" }))
        var fullKeyStr = ""
        for (i in 1 until fullKeyLength) {
            if (i %2 == 1) {
                computeRule30Bool(bufferA, bufferB)
                fullKeyStr += bufferB[fullKeyColumn]
                Log.e("000", "binarized=" + bufferB.joinToString(separator = "", transform = { b -> if (b) "1" else "0" }))
            } else {
                computeRule30Bool(bufferB, bufferA)
                fullKeyStr += bufferA[fullKeyColumn]
                Log.e("000", "binarized=" + bufferA.joinToString(separator = "", transform = { b -> if (b) "1" else "0" }))
            }
        }

        Log.e("000", "fullKey=$fullKeyStr")

        text.text = "It's working!"
    }

    fun computeRule30Bool(input: BooleanArray, output: BooleanArray) {
//        output.fill(true)
        for (i in 1 until input.size - 1) {
            val prev = input[i - 1]
            val current = input[i]
            val next = input[i + 1]
            output[i] = rule30(prev, current, next)
        }
    }

    fun rule30(a: Boolean, b: Boolean, c: Boolean): Boolean {
        //return (a == b == c) || (!a && (b xor c))
        if (!a && !b && !c) return true
        if (!a && !b && c) return true
        if (!a && b && !c) return true
        if (a && b && c) return true
        return false
    }
}
