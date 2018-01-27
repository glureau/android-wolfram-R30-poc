package com.glureau.wolfram30.rx

// https://github.com/ReactiveX/RxJavaString/blob/1.x/src/main/java/rx/internal/operators/OnSubscribeInputStream.java

import io.reactivex.Emitter
import io.reactivex.Flowable
import io.reactivex.functions.BiConsumer
import io.reactivex.functions.Consumer
import java.io.InputStream
import java.util.*
import java.util.concurrent.Callable


object FlowableUtils {
    private val BUFFER_SIZE = 1024

    fun generate(inputStream: InputStream, bufferSize: Int = BUFFER_SIZE): Flowable<ByteArray> {
        val buffer = ByteArray(bufferSize)
        // Need to define Callable/BiConsumer/Consumer to fix ambiguities
        return Flowable.generate<ByteArray, InputStream>(
                Callable { inputStream },
                BiConsumer { input: InputStream, emitter: Emitter<ByteArray> ->
                    val available = minOf(input.available(), bufferSize)
                    if (available > 0) {
                        input.read(buffer, 0, available)
                        if (available < bufferSize) {
                            val shortBuffer = Arrays.copyOfRange(buffer, 0, available)
                            emitter.onNext(shortBuffer)
                        } else {
                            emitter.onNext(buffer)
                        }
                    } else {
                        emitter.onComplete()
                    }
                },
                Consumer { input: InputStream -> input.close() }
        )
    }
}