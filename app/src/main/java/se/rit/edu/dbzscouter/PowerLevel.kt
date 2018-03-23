package se.rit.edu.dbzscouter

/**
 * Stores information relevant to the power level calculation
 * @author Curtis Shea
 * @author Gavriel Rachael-Homann
 */
class PowerLevel(var micRecorder: IMicRecorder) {

    private var faceGraphic: FaceGraphic? = null

    /**
     * Calculates the power level
     * @return the power level
     */
    fun calculatePowerLevel(): Int {
        var powerLevel = 0.0


        // fluctuates between 0 and DECIBEL_WEIGHT percent of BASE_POWER_LEVEL, based on
        // the decibels above min of MicRecorder
        powerLevel += (((micRecorder.maxReasonableDecibels - micRecorder.decibelsAboveMin) / micRecorder.maxReasonableDecibels) * BASE_POWER_LEVEL * DECIBEL_WEIGHT)

        // fluctuates between 0 and EMOTION_WEIGHT percent of BASE_POWER_LEVEL, based on
        // the emotion val of FaceGraphic
        powerLevel += (faceGraphic!!.getEmotionVal() * BASE_POWER_LEVEL * EMOTION_WEIGHT)

        return powerLevel.toInt()
    }

    fun setFaceGraphic(faceGraphic: FaceGraphic){
        this.faceGraphic = faceGraphic
    }

    fun getBasePowerLevel() : Int{
        return BASE_POWER_LEVEL
    }

    companion object {
        private val BASE_POWER_LEVEL = 50000
        private val EMOTION_WEIGHT = 0.9
        private val DECIBEL_WEIGHT = 0.1
    }
}
