package com.glureau.wolfram30

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.glureau.wolfram30.storage.AndroidSecurePreferences
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        text.text = "Creating encryption..."

        val encryption: Encryption = WolframAutomataRule30Encryption(AndroidSecurePreferences())
        // Should retrieve and display the key
        val encryptionKey = encryption.generateInitialKey("toto_room")

        val originalMessage = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer pretium, arcu ut bibendum facilisis, ex sapien posuere quam, ut cursus eros lectus sit amet orci. Aliquam malesuada eleifend viverra. Donec nec lorem libero. Aenean id arcu turpis. Vivamus diam ex, tristique non commodo vel, venenatis sed metus. Sed sit amet luctus ante. Proin et facilisis sapien. Morbi hendrerit augue arcu, id placerat nisi auctor sit amet. Nullam sed augue accumsan, vestibulum velit nec, laoreet ligula cras amet."
        val originalBitSet = OBitSet.valueOf(originalMessage.toByteArray())

        foo(encryption, originalBitSet, originalMessage)
    }

    fun foo(encryption: Encryption, originalBitSet: OBitSet, originalMessage: String) {
        val startTime = System.currentTimeMillis()
        val encryptedString = OBitSet(originalBitSet.bitCount())
        encryption.encrypt("toto_room", originalBitSet, encryptedString)
                .sample(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ progression ->
                    //                    text.text = "Encryption... $progression%"
                }, { error ->
                    text.text = "Encryption Error... $error"
                }, {
                    //                    text.text = "Decryption..."
                    val finalMessage = OBitSet(encryptedString.bitCount())
                    encryption.encrypt("toto_room", encryptedString, finalMessage)
                            .sample(1000, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ progress ->
                                //                                text.text = "Decryption... $progress%"
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
                                foo(encryption, originalBitSet, originalMessage)
                            })
                })
    }
}
