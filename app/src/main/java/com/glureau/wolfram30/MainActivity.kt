package com.glureau.wolfram30

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.glureau.wolfram30.encryption.*
import com.glureau.wolfram30.rx.FlowableUtils
import com.glureau.wolfram30.storage.AndroidSecurePreferences
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    companion object {
        // SIZE = 1024 char
        val originalMessage = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed condimentum tellus sed semper euismod. Integer dignissim eros tellus, et sagittis tellus sagittis at. " +
                "Donec vehicula tortor a augue tincidunt, ut auctor nulla feugiat. Vivamus luctus nulla mauris, vel iaculis felis rhoncus a. Pellentesque id orci eu lorem hendrerit finibus id at mi. " +
                "Aenean velit purus, porta at lacus ut, rutrum posuere sapien. Nunc fringilla egestas sollicitudin. Quisque sit amet orci ut purus aliquet luctus et vel urna. Nullam vel efficitur arcu, posuere condimentum libero. " +
                "Curabitur vitae odio non felis interdum pulvinar eu at purus. Nunc vel finibus felis.\n\n" +
                "Cras vitae dolor lacus. Nunc tempus, mi a venenatis venenatis, quam lacus tempor nibh, nec fringilla nisl sapien quis lectus. Interdum et malesuada fames ac ante ipsum primis in faucibus." +
                " Mauris pharetra odio a lectus varius commodo. Fusce tincidunt tellus vel ex sodales, in sollicitudin mi mollis. Integer rutrum lacus id mi pretium luctus. Mauris eget libero magna. Morbi metus."
        val readingBufferSize = 1024
        val encryptionKeyId = "encrypt_id"
        val decryptionKeyId = "decrypt_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainLabel.textSize = 10f
        mainLabel.text = "Creating encryption..."

        val encryption: Encryption = WolframAutomataRule30Encryption(AndroidSecurePreferences())
        val encryptionKey = encryption.generateInitialKey(encryptionKeyId)

        val decryption: Decryption = WolframAutomataRule30Encryption(AndroidSecurePreferences())
        decryption.setEncryptionKey(decryptionKeyId, encryptionKey)
        go(encryption, decryption)
    }

    private fun go(encryption: Encryption, decryption: Decryption) {
        encryptTextStream(encryption, decryption, originalMessage, {
            encryptImageStream(encryption, decryption, resources.openRawResource(R.raw.stephen_wolfram), {
                Thread.sleep(5000)
                go(encryption, decryption)
            })
        })
    }

    fun encryptTextStream(encryption: Encryption, decryption: Decryption, message: String, callbackFinish: () -> Unit) {
        val startTime = System.currentTimeMillis()

        var userMessage = "Encrypted message = "
        mainLabel.text = userMessage
        encryptDecryptInputStream(encryption, decryption, message.toInputStream(),
                { byteArrays ->
                    byteArrays.forEach { byteArray ->
                        byteArray.forEach { b ->
                            userMessage += b.toChar()
                        }
                    }
                    mainLabel.text = userMessage
                },
                {
                    mainLabel.text = "Encryption Error... $it"
                    it.printStackTrace()
                },
                {
                    userMessage += "\n\n Decrypted message = "
                    mainLabel.text = userMessage
                },
                { byteArrays ->
                    byteArrays.forEach { byteArray ->
                        byteArray.forEach { b ->
                            userMessage += b.toChar()
                        }
                    }

                    mainLabel.text = userMessage
                },
                {
                    mainLabel.text = "Decryption Error... $it"
                    it.printStackTrace()
                },
                {
                    userMessage += "\n\nTotal duration= ${System.currentTimeMillis() - startTime}ms"
                    mainLabel.text = userMessage
                    callbackFinish()
                })
    }


    fun encryptImageStream(encryption: Encryption, decryption: Decryption, inputStream: InputStream, callbackFinish: () -> Unit) {
        val startTime = System.currentTimeMillis()

//        imageLabel.text = "Encrypting image..."
        imageView.setImageResource(R.drawable.noise_314_220) // Fake image as the real image isn't a valid bitmap
        val imageBuffer = ByteArrayOutputStream()

        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888

        encryptDecryptInputStream(encryption, decryption, inputStream,
                { },
                {
                    imageLabel.text = "Encryption Error... $it"
                    it.printStackTrace()
                },
                {
                    imageLabel.text = "Decrypting image..."
                },
                { byteArrays ->
                    byteArrays.forEach { imageBuffer.write(it) }

                    val resultByteArray = imageBuffer.toByteArray()
                    val bitmap = BitmapFactory.decodeByteArray(resultByteArray, 0, resultByteArray.size)

                    imageView.setImageBitmap(bitmap)
                },
                {
                    imageLabel.text = "Decryption Error... $it"
                    it.printStackTrace()
                },
                {
                    val resultByteArray = imageBuffer.toByteArray()
                    val bitmap = BitmapFactory.decodeByteArray(resultByteArray, 0, resultByteArray.size)

                    imageView.setImageBitmap(bitmap)
                    imageLabel.text = "Total duration = ${System.currentTimeMillis() - startTime} ms"
                    callbackFinish()
                }
        )
    }

    fun encryptDecryptInputStream(encryption: Encryption, decryption: Decryption,
                                  inputStream: InputStream,
                                  callbackEncrypt: (List<ByteArray>) -> Unit,
                                  callbackEncryptError: (Throwable) -> Unit,
                                  callbackStartDecryption: () -> Unit,
                                  callbackDecrypt: (List<ByteArray>) -> Unit,
                                  callbackDecryptError: (Throwable) -> Unit,
                                  callbackFinish: () -> Unit
    ) {
        val encryptedMessage = ByteArrayOutputStream()

        FlowableUtils.generate(inputStream, readingBufferSize)
                .compose { encryption.encrypt(encryptionKeyId, it) }
                .buffer(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ byteArrays ->
                    byteArrays.forEach { byteArray ->
                        encryptedMessage.write(byteArray)
                    }
                    callbackEncrypt(byteArrays)
                }, { error ->
                    callbackEncryptError(error)
                }, {
                    callbackStartDecryption()
                    FlowableUtils.generate(encryptedMessage.toByteArray().inputStream(), readingBufferSize)
                            .compose { decryption.decrypt(decryptionKeyId, it) }
                            .buffer(500, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ byteArrays ->
                                callbackDecrypt(byteArrays)
                            }, { error ->
                                callbackDecryptError(error)
                            }, {
                                callbackFinish()
                            })
                })
    }
}
