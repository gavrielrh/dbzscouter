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
        var powerLevelPercentage = 0.0

        // fluctuates between 0 and DECIBEL_WEIGHT percent of BASE_POWER_LEVEL, based on
        // the decibels above min of MicRecorder
         var decibelPower = (((micRecorder.maxReasonableDecibels - micRecorder.decibelsAboveMin) / micRecorder.maxReasonableDecibels))


        // if the amount decibels would contribute to the power level is less than the minimum threshold
        // don't have it contribute to power level
        if(decibelPower < MIN_DECIBEL_POWER_THRESHOLD){
            decibelPower = 0.0
        }

        powerLevelPercentage += decibelPower * DECIBEL_WEIGHT

        // fluctuates between 0 and EMOTION_WEIGHT percent of BASE_POWER_LEVEL, based on
        // the emotion val of FaceGraphic
        powerLevelPercentage += (faceGraphic!!.getEmotionVal() * EMOTION_WEIGHT)

        powerLevel = BASE_POWER_LEVEL * Math.pow(powerLevelPercentage, POWER_EXP_CONST)

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
        private val POWER_EXP_CONST = 2.0
        private val EMOTION_WEIGHT = 0.9
        private val DECIBEL_WEIGHT = 0.1
        private val MIN_DECIBEL_POWER_THRESHOLD = 0.3
    }
}
