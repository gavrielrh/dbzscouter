package se.rit.edu.dbzscouter;


import java.util.Random;

/**
 * Stores information relevant to the power level calculation
 */
public class PowerLevel{
    private MicRecorder micRecorder;
    private FaceGraphic faceGraphic;

    private static final int EMOTION_WEIGHT = 50;
    private static final int DECIBEL_WEIGHT = 50;

    public PowerLevel(MicRecorder micRecorder){
        this.micRecorder = micRecorder;
    }

    public void setFaceGraphic(FaceGraphic faceGraphic){
        this.faceGraphic = faceGraphic;
    }

    /**
     * Calculates the power level
     * @return the power level
     */
    public int calculatePowerLevel(){
        int powerLevel = 2500;

        powerLevel += micRecorder.getNumDecibelsAboveMin() * DECIBEL_WEIGHT;
        powerLevel += (faceGraphic.getEmotionVal() * 100) * EMOTION_WEIGHT;

        return powerLevel;
    }
}
