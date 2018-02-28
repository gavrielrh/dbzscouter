package se.rit.edu.dbzscouter

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.R.attr.data
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.DoubleStream


/**
 * Used for getting decibels from the mic.
 * @author Curtis Shea
 * Date of Creation: 2/23/18
 */
class MicRecorder : AudioRecord {
    companion object {
        @JvmStatic val SAMPLE_RATE = 44100;
    }

    private val soundLevelData = AtomicLong()
    var soundLevel: Double
        get() = java.lang.Double.longBitsToDouble(soundLevelData.get())
        set(value) { soundLevelData.set(java.lang.Double.doubleToRawLongBits(value)) }

    // Constructor, sets up super using:
    // audio source from mic
    // in 44100 HZ, works for every device
    // channel config MONO, works on all devices
    // audio format as 16 bit short
    // buffer size in bytes
    constructor() : super(MediaRecorder.AudioSource.DEFAULT,
                          SAMPLE_RATE,
                          AudioFormat.CHANNEL_IN_MONO,
                          AudioFormat.ENCODING_PCM_16BIT,
                          AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)){

    }

    /**
     * Get the current decibles from the recorder
     * @return the current decibles
     */
    fun getDecibles() : Double{
        val shortBuffer = ShortArray(AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)) // want to pull at the 44100th byte sampling in HZ.
        this.read(shortBuffer, 0, shortBuffer.size)
        val sample : Double = shortBuffer.max()?.toDouble() ?: Double.NEGATIVE_INFINITY
        soundLevel = if (sample == 0.0) {
            Double.NEGATIVE_INFINITY
        } else {
            20.0 * Math.log10(sample / 65535.0)
        }
        return soundLevel
    }

    /**
     * Performs functions to close the mic.
     */
    fun close(){
        this.stop()
        this.release()
    }
}