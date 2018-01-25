package com.glureau.wolfram30

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        text.text = "Creating encryption..."

        val encryption: Encryption = WolframAutomataRule30Encryption()
        // Should retrieve and display the key
        encryption.generateInitialKey("toto_room")

        text.text = "Encryption..."

        // 2s for 500 chars
        val originalMessage = "The quick, brown fox jumps over a lazy dog. DJs flock by when MTV ax quiz prog. Junk MTV quiz graced by fox whelps. Bawds jog, flick quartz, vex nymphs. Waltz, bad nymph, for quick jigs vex! Fox nymphs grab quick-jived waltz. Brick quiz whangs jumpy veldt fox. Bright vixens jump; dozy fowl quack. Qu"
        Log.e("OOO", "originalMessage=$originalMessage")

        val originalBitSet = BitSet.valueOf(originalMessage.toByteArray())
//        val b64 = Base64.decode(originalMessage)
        Log.e("OOO", "originalBitSet=$originalBitSet")
        val encryptedString = BitSet(originalBitSet.size())

        encryption.encrypt("toto_room", originalBitSet, encryptedString)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ progression ->
                    text.text = "Encryption... $progression%"
                }, { error ->
                    text.text = "Error... $error"
                }, {
                    Log.e("OOO", "encryptedString=$encryptedString")
                    Log.e("OOO", "base64(encryptedString)=${encryptedString.toBase64()}")

                    text.text = "Decryption..."
                    val finalMessage = BitSet(encryptedString.size())
                    encryption.encrypt("toto_room", encryptedString, finalMessage).blockingSubscribe()


                    Log.e("OOO", "String(finalMessage.toByteArray())=${String(finalMessage.toByteArray())}")
                    val bytes = finalMessage.toByteArray()

//                    finalMessage.toByteArray().joinToString { '\0'+it) }
                    text.text = "Original message = $originalMessage \n\n" +
                            "Encrypted message = ${encryptedString.toBase64()} \n\n" +
                            "Decrypted message = ${String(finalMessage.toByteArray())}"
                })

/*

        val startTime = System.currentTimeMillis()
//        val message = "abcdef".toByteArray()
        //val privateKey = "12345".toByteArray()

        // Random data
        val messageSize = 8 * 1024
        val message = BitSet(messageSize)
        val rand = Random(1)
        for (i in 0..messageSize) {
            message[i] = rand.nextBoolean()
        }

        // Crypto-securely generate a private key (use this method for automatic key generation?)
        val privateKeySize = 1024
        val privateKey = BitSet(privateKeySize)
        for (i in 0..privateKeySize) {
            privateKey[i] = rand.nextBoolean()
        }

        val triangleWidth = privateKeySize + messageSize * 2
        val fullKeyColumn = (triangleWidth / 2) + 1

        // Prepare the memory for computation
        val bufferA = BitSet(triangleWidth)
        val bufferB = BitSet(triangleWidth)

        // Initialize the first line
        bufferA.set(0, triangleWidth, true)
        for (i in 0..privateKeySize) {
            bufferA[messageSize + i] = privateKey[i]
        }

        Log.e("000", "binarized=" + bufferA.toString())
        val fullKey = BitSet(messageSize)
        for (i in 1 until messageSize) {
            if (i % 2 == 1) {
                computeRule30Bool(bufferA, bufferB, triangleWidth)
                fullKey[i] = bufferB[fullKeyColumn]
            } else {
                computeRule30Bool(bufferB, bufferA, triangleWidth)
                fullKey[i] = bufferA[fullKeyColumn]
            }
            if (i % 100 == 0) {
                Log.e("000", "progression= $i/$messageSize (${(i.toFloat() * 100f) / messageSize.toFloat()}%)")
            }
        }

        Log.e("000", "fullKey=${fullKey.toBinaryString(messageSize)}")
*/
    }

//    fun computeRule30Bool(input: BitSet, output: BitSet, bufferSize: Int) {
//        for (i in 1 until bufferSize - 1) {
//            val prev = input[i - 1]
//            val current = input[i]
//            val next = input[i + 1]
//            output[i] = rule30(prev, current, next)
//        }
//    }
//
//    fun rule30(a: Boolean, b: Boolean, c: Boolean): Boolean {
//        //return (a == b == c) || (!a && (b xor c))
//        if (!a && !b && !c) return true
//        if (!a && !b && c) return true
//        if (!a && b && !c) return true
//        if (a && b && c) return true
//        return false
//    }
}
