package se.rit.edu.dbzscouter

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.R.attr.data
import java.util.stream.DoubleStream


/**
 * Used for getting decibels from the mic.
 * @author Curtis Shea
 * Date of Creation: 2/23/18
 */
class MicRecorder : AudioRecord {


    // Constructor, sets up super using:
    // audio source from mic
    // in 44100 HZ, works for every device
    // channel config MONO, works on all devices
    // audio format as 16 bit short
    // buffer size in bytes
    constructor() : super(MediaRecorder.AudioSource.MIC,
                          44100,
                          AudioFormat.CHANNEL_IN_MONO,
                          AudioFormat.ENCODING_PCM_16BIT,
                          AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)){
    }

    /**
     * Get the current decibles from the recorder
     * @return the current decibles
     */
    fun getDecibles() : Double{
        var shortBuffer = ShortArray(1024)
        this.read(shortBuffer, 0, 1024)
        var p2 : Double = shortBuffer[shortBuffer.lastIndex].toDouble()

        val decibel: Double

        if (p2 == 0.0)
            decibel = java.lang.Double.NEGATIVE_INFINITY
        else
            decibel = 20.0 * Math.log10(p2 / 65535.0)

        return decibel
    }

    /**
     * Performs functions to close the mic.
     */
    fun close(){
        this.stop()
        this.release()
    }
}