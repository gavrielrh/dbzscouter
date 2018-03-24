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

        // fluctuates between 0 and DECIBEL_WEIGHT of 1, based on
        // the decibels above min of MicRecorder
         var decibelPower = (((micRecorder.maxReasonableDecibels - micRecorder.decibelsAboveMin) / micRecorder.maxReasonableDecibels))

        // cap decibel power to 1
        if(decibelPower > 1){
            decibelPower = 1.0
        }

        // if the amount decibels would contribute to the power level is less than the minimum threshold
        // don't have it contribute to power level
        if(decibelPower < MIN_DECIBEL_POWER_THRESHOLD){
            decibelPower = 0.0
        }

        //
        powerLevelPercentage += decibelPower * DECIBEL_WEIGHT

        // fluctuates between 0 and EMOTION_WEIGHT percent of 1, based on
        // the emotion val of FaceGraphic
        powerLevelPercentage += (faceGraphic!!.getEmotionVal() * EMOTION_WEIGHT)


        // make the powerlevel an exponential curve, from 0 to 1.
        powerLevel = MAX_POWER_LEVEL * Math.pow(powerLevelPercentage, POWER_EXP_CONST)

        return powerLevel.toInt()
    }

    fun setFaceGraphic(faceGraphic: FaceGraphic){
        this.faceGraphic = faceGraphic
    }

    fun getMaxPowerLevel() : Int{
        return MAX_POWER_LEVEL
    }

    companion object {
        // the max power level.
        private val MAX_POWER_LEVEL = 50000

        // Used to determine the power level curve.
        // Make POWER_EXP_CONST a higher number for a steeper curve,
        // and a lower number for a more linear curve.
        // Must be at least 1, which would be a straight line.
        // Between the range of 2.0 and 5.0 is recommended.
        private val POWER_EXP_CONST = 2.0

        // between 0 and 1, representing the min value of
        // decibel power level pre-weighting that we will actually add to powerLevelPercentage
        private val MIN_DECIBEL_POWER_THRESHOLD = 0.3


        // constant weights that affect their respective contributions to MAX_POWER_LEVEL
        // each one must be a double between 0 and 1, and they must all sum to 1.
        private val EMOTION_WEIGHT = 0.9
        private val DECIBEL_WEIGHT = 0.1
    }
}
