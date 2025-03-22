package com.rejeq.cpcam.microbenchmarks

import androidx.benchmark.BlackHole
import androidx.benchmark.ExperimentalBlackHoleApi
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.nio.ByteBuffer
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalBlackHoleApi::class)
@RunWith(AndroidJUnit4::class)
class ByteBufferBenchmark {
    val bufferCapacity: Int = 1920 * 1080 * 4

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    val byteArray = ByteArray(bufferCapacity) { (it % 256).toByte() }
    val wrappedByteBuffer = ByteBuffer.wrap(byteArray)

    val byteBuffer = ByteBuffer.allocate(bufferCapacity)
    val directByteBuffer = ByteBuffer.allocateDirect(bufferCapacity)

    init {
        System.loadLibrary("cpcam_bench_jni")

        for (i in 0 until bufferCapacity) {
            byteBuffer.put(i, (i % 256).toByte())
            directByteBuffer.put(i, (i % 256).toByte())
        }

        allocateNativeBuffer(bufferCapacity)
    }

    @Test
    fun measureByteArrayRead() {
        benchmarkRule.measureRepeated {
            var sum: Long = 0

            for (byte in byteArray) {
                sum += byte
            }

            BlackHole.consume(sum)
        }
    }

    @Test
    fun measureWrappedByteBufferRead() {
        benchmarkRule.measureRepeated {
            var sum: Long = 0
            wrappedByteBuffer.clear()

            while (wrappedByteBuffer.hasRemaining()) {
                sum += wrappedByteBuffer.get()
            }

            BlackHole.consume(sum)
        }
    }

    @Test
    fun measureByteBufferAsArrayRead() {
        benchmarkRule.measureRepeated {
            var sum: Long = 0
            wrappedByteBuffer.clear()

            val array = wrappedByteBuffer.array()
            for (byte in array) {
                sum += byte
            }

            BlackHole.consume(sum)
        }
    }

    @Test
    fun measureCopiedByteBufferRead() {
        benchmarkRule.measureRepeated {
            var sum: Long = 0
            wrappedByteBuffer.clear()

            val array = ByteArray(directByteBuffer.remaining())
            for (byte in array) {
                sum += byte
            }

            BlackHole.consume(sum)
        }
    }

    @Test
    fun measureByteBufferRead() {
        benchmarkRule.measureRepeated {
            var sum: Long = 0
            byteBuffer.clear()

            while (byteBuffer.hasRemaining()) {
                sum += byteBuffer.get()
            }

            BlackHole.consume(sum)
        }
    }

    @Test
    fun measureByteBufferReadV2() {
        benchmarkRule.measureRepeated {
            var sum: Long = 0

            for (i in 0 until bufferCapacity) {
                sum += byteBuffer.get(i)
            }

            BlackHole.consume(sum)
        }
    }

    @Test
    fun measureDirectByteBufferRead() {
        benchmarkRule.measureRepeated {
            var sum: Long = 0
            directByteBuffer.clear()

            while (directByteBuffer.hasRemaining()) {
                sum += directByteBuffer.get()
            }

            BlackHole.consume(sum)
        }
    }

    @Test
    fun measureNativeBufferRead() {
        benchmarkRule.measureRepeated {
            val sum = nativeBufferRead()
            BlackHole.consume(sum)
        }
    }

    @Test
    fun measureNativeDirectByteBufferRead() {
        benchmarkRule.measureRepeated {
            val sum = nativeDirectByteBufferRead(directByteBuffer)
            BlackHole.consume(sum)
        }
    }

    external fun allocateNativeBuffer(capacity: Int)
    external fun cleanupNativeBuffer()
    external fun nativeBufferRead(): Long
    external fun nativeDirectByteBufferRead(buffer: ByteBuffer): Long
}
