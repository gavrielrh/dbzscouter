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

    // the current decibel reading
    private val soundLevelData = AtomicLong()
    var soundLevel: Double
        get() = java.lang.Double.longBitsToDouble(soundLevelData.get())
        set(value) { soundLevelData.set(java.lang.Double.doubleToRawLongBits(value)) }

    // the current minimum value sound level has ever had
    private val minSoundLevelData = AtomicLong()
    var minSoundLevel: Double
        get() = java.lang.Double.longBitsToDouble(minSoundLevelData.get())
        set(value) { minSoundLevelData.set(java.lang.Double.doubleToRawLongBits(value)) }


    // whether or not the thread should keep running
    private val runningData = AtomicBoolean(false)
    public var running: Boolean
        get() = runningData.get()
        set(value) { runningData.set(value) }

    // the buffersize to read audio data to
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

            // TODO Geting NaN for min and average, which is throwing everything off
            // val min = decibleConv(shortBuffer.min()?.toDouble()) ?: averageMin
            val max = decibleConv(shortBuffer.max()?.toDouble()) ?: averageMax
            // val avg = decibleConv(shortBuffer.average())!!

            // averageMin = (averageMin + min) / 2
            averageMax = (averageMax + max) / 2

            // soundLevel = (avg - averageMin) / ( averageMax - averageMin)

            soundLevel = averageMax

            if(soundLevel < minSoundLevel ){
                minSoundLevel = soundLevel
            }else if(minSoundLevel == 0.0){
                minSoundLevel = soundLevel
            }

            // De-Comment for Debugging
            //System.err.format("Sound data: Min: %.3f Max: %.3f Avg: %.3f SoundLevel: %.3f\n", min, max, avg, soundLevel)
            //System.err.format("Sound data: Decibels: %.3f Minimum: %.3f \n", soundLevel, minSoundLevel)


            // Don't eat all the CPU, this is a phone after all
            Thread.sleep(5)
        }
        recorder.stop()
    }

    /**
     * Used to convert short values to decibels
     */
    private fun decibleConv(sample: Double?): Double? {
        sample ?: return null

        // should be negative infinity, but that causes problems.
        // and if the max is 0, we've probably got bad data
        if(sample == 0.0) {
            return soundLevel
        }

        return 20.0 * Math.log10(sample / 65535.0)
    }

    /**
     * Used to retrieve the number of decibels above the minimum recorded decibel level
     */
    fun getNumDecibelsAboveMin() : Double?{
        return soundLevel - minSoundLevel
    }
}