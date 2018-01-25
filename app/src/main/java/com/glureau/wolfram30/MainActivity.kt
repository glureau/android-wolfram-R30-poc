package com.glureau.wolfram30

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startTime = System.currentTimeMillis()

        text.text = "Creating encryption..."

        val encryption: Encryption = WolframAutomataRule30Encryption()
        // Should retrieve and display the key
        val encryptionKey = encryption.generateInitialKey("toto_room")

        // >5.5s for 300 chars (encrypt+decrypt)
        val originalMessage = "The quick, brown fox jumps over a lazy dog. DJs flock by when MTV ax quiz prog. Junk MTV quiz graced by fox whelps. Bawds jog, flick quartz, vex nymphs. Waltz, bad nymph, for quick jigs vex! Fox nymph"
        val originalBitSet = BitSet.valueOf(originalMessage.toByteArray())
        val encryptedString = BitSet(originalBitSet.size())

        encryption.encrypt("toto_room", originalBitSet, encryptedString)
                .sample(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ progression ->
                    text.text = "Encryption... $progression%"
                }, { error ->
                    text.text = "Encryption Error... $error"
                }, {
//                    text.text = "Decryption..."
                    val finalMessage = BitSet(encryptedString.size())
                    encryption.encrypt("toto_room", encryptedString, finalMessage)
                            .sample(100, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ progress ->
                                text.text = "Decryption... $progress%"
                            }, { error ->
                                text.text = "Decryption Error... $error"
                            }, {
                                val userMessage = "Original message = $originalMessage \n\n" +
//                                        "Encryption key = ${encryptionKey.toBase64()} \n\n" +
                                        "Encrypted message = ${encryptedString.toBase64()} \n\n" +
                                        "Decrypted message = ${String(finalMessage.toByteArray())}\n\n" +
                                        "Total duration = ${System.currentTimeMillis() - startTime} ms"
                                Log.e("OOO", userMessage)
                                text.text = userMessage
                            })
                })
    }
}
