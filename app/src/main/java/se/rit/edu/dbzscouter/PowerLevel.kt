package se.rit.edu.dbzscouter

/**
 * Stores information relevant to the power level calculation
 */
class PowerLevel(var micRecorder: IMicRecorder) {

    var faceGraphic: FaceGraphic? = null

    /**
     * Calculates the power level
     * @return the power level
     */
    fun calculatePowerLevel(): Int {
        var powerLevel = 0

        powerLevel += (micRecorder.decibelsAboveMin * DECIBEL_WEIGHT).toInt()
        powerLevel += (faceGraphic!!.getEmotionVal() * 100 * EMOTION_WEIGHT).toInt()

        return powerLevel
    }

    companion object {

        private val EMOTION_WEIGHT = 50
        private val DECIBEL_WEIGHT = 50
    }
}
