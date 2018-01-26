package com.glureau.wolfram30

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.glureau.wolfram30.storage.AndroidSecurePreferences
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.io.DataInputStream
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        mainLabel.textSize = 10f
        mainLabel.text = "Creating encryption..."

        val encryption: Encryption = WolframAutomataRule30Encryption(AndroidSecurePreferences())
        // Should retrieve and display the key
        val encryptionKey = encryption.generateInitialKey("toto_room")

        encryptText(encryption)
    }

    fun encryptText(encryption: Encryption) {
        // SIZE = 1024 char
        val originalMessage = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed condimentum tellus sed semper euismod. Integer dignissim eros tellus, et sagittis tellus sagittis at. " +
                "Donec vehicula tortor a augue tincidunt, ut auctor nulla feugiat. Vivamus luctus nulla mauris, vel iaculis felis rhoncus a. Pellentesque id orci eu lorem hendrerit finibus id at mi. " +
                "Aenean velit purus, porta at lacus ut, rutrum posuere sapien. Nunc fringilla egestas sollicitudin. Quisque sit amet orci ut purus aliquet luctus et vel urna. Nullam vel efficitur arcu, posuere condimentum libero. " +
                "Curabitur vitae odio non felis interdum pulvinar eu at purus. Nunc vel finibus felis.\n\n" +
                "Cras vitae dolor lacus. Nunc tempus, mi a venenatis venenatis, quam lacus tempor nibh, nec fringilla nisl sapien quis lectus. Interdum et malesuada fames ac ante ipsum primis in faucibus." +
                " Mauris pharetra odio a lectus varius commodo. Fusce tincidunt tellus vel ex sodales, in sollicitudin mi mollis. Integer rutrum lacus id mi pretium luctus. Mauris eget libero magna. Morbi metus."

        val originalBitSet = OBitSet.valueOf(originalMessage.toByteArray())

        val startTime = System.currentTimeMillis()
        val encryptedString = OBitSet(originalBitSet.bitCount())
        encryption.encrypt("toto_room", originalBitSet, encryptedString)
                .sample(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ progression ->
                    //                    text.text = "Encryption... $progression%"
                }, { error ->
                    mainLabel.text = "Encryption Error... $error"
                }, {
                    //                    text.text = "Decryption..."
                    val finalMessage = OBitSet(encryptedString.bitCount())
                    encryption.encrypt("toto_room", encryptedString, finalMessage)
                            .sample(1000, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ progress ->
                                //                                text.text = "Decryption... $progress%"
                            }, { error ->
                                mainLabel.text = "Decryption Error... $error"
                            }, {
                                val userMessage = "Original message = $originalMessage \n\n" +
//                                        "Encryption key = ${encryptionKey.toBase64()} \n\n" +
//                                        "Encrypted message = ${encryptedString.toBase64()} \n\n" +
                                        "Decrypted message = ${String(finalMessage.toByteArray())}\n\n" +
                                        "Total duration = ${System.currentTimeMillis() - startTime} ms"
                                Log.e("OOO", userMessage)
                                mainLabel.text = userMessage
                                encryptImage(encryption)
                            })
                })
    }


    fun encryptImage(encryption: Encryption) {
        val inputStream = resources.openRawResource(R.raw.stephen_wolfram)
        val buffer = ByteArray(inputStream.available())
        Log.wtf("OOO", "Original buffer size = ${buffer.size}")
        DataInputStream(inputStream).readFully(buffer)
        val originalBitSet = OBitSet.valueOf(buffer)

        val startTime = System.currentTimeMillis()
        val encryptedString = OBitSet(originalBitSet.bitCount())
        encryption.encrypt("toto_room", originalBitSet, encryptedString)
                .sample(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ progression ->
                    //                    text.text = "Encryption... $progression%"
                }, { error ->
                    mainLabel.text = "Encryption Error... $error"
                }, {
                    //                    text.text = "Decryption..."
                    val finalMessage = OBitSet(encryptedString.bitCount())
                    encryption.encrypt("toto_room", encryptedString, finalMessage)
                            .sample(1000, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ progress ->
                                //                                text.text = "Decryption... $progress%"
                            }, { error ->
                                mainLabel.text = "Decryption Error... $error"
                            }, {
                                val options = BitmapFactory.Options()
                                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                                val resultByteArray = finalMessage.toByteArray()
                                Log.wtf("OOO", "Original buffer size = ${resultByteArray.size}")
                                val bitmap = BitmapFactory.decodeByteArray(resultByteArray, 0, resultByteArray.size)

                                imageView.setImageBitmap(bitmap)
                                imageLabel.text = "Total duration = ${System.currentTimeMillis() - startTime} ms"
                                encryptText(encryption)
                            })
                })
    }
}
