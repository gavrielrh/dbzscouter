package se.rit.edu.dbzscouter

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong


/**
 * Used for getting decibels from the mic.
 * @author Curtis Shea
 * Date of Creation: 2/23/18
 */
class MicRecorder: Runnable {
    companion object {
        @JvmStatic val SAMPLE_RATE = 44100;
    }

    private val soundLevelData = AtomicLong()
    var soundLevel: Double
        get() = java.lang.Double.longBitsToDouble(soundLevelData.get())
        set(value) { soundLevelData.set(java.lang.Double.doubleToRawLongBits(value)) }

    private val runningData = AtomicBoolean(false)
    public var running: Boolean
        get() = runningData.get()
        set(value) { runningData.set(value) }

    private val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT);

    /**
     * Recorder which reads from the mic
     * in 44100 HZ, works for every device
     * channel config MONO, works on all devices
     * audio format as 16 bit short
     * buffer size in bytes
     */
    private val recorder = AudioRecord(
            MediaRecorder.AudioSource.DEFAULT,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize)

    /**
     * Run the audio listener thread.
     */
    override fun run() {
        recorder.startRecording()

        val shortBuffer = ShortArray(bufferSize)
        var averageMin = 0.0
        var averageMax = 0.0
        running = true
        while (running) {
            recorder.read(shortBuffer, 0, shortBuffer.size)
            val min = decibleConv(shortBuffer.min()?.toDouble()) ?: averageMin
            val max = decibleConv(shortBuffer.max()?.toDouble()) ?: averageMax
            val avg = decibleConv(shortBuffer.average())!!
            averageMin = (averageMin + min) / 2
            averageMax = (averageMax + max) / 2
            soundLevel = (avg - averageMin) / (averageMax - averageMin)

            // De-Comment for Debugging
            //System.err.println("Decibels: " + soundLevel)

            // Don't eat all the CPU, this is a phone after all
            Thread.sleep(5)
        }

        recorder.stop()
        recorder.release()
    }

    private fun decibleConv(sample: Double?): Double? {
        sample ?: return null
        return 20.0 * Math.log10(sample / 65535.0)
    }
}