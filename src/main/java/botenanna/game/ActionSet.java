package botenanna.game;

import rlbot.ControllerState;
import botenanna.ControlsOutput;

/**
 * A data class describing the outputs of an agent. This class can be translated into a ControllerState.
 */
public class ActionSet {

    // 0 is straight, -1 is hard left, 1 is hard right.
    private double steeringTilt = 0;

    // -1 for front flip, 1 for back flip
    private double pitchTilt = 0;
    private double rollTilt = 0;

    // 1 is forwards, -1 is backwards
    private double throttle = 0;

    private boolean jumpDepressed = false;
    private boolean boostDepressed = false;
    private boolean slideDepressed = false;

    public ActionSet() {
    }

    /** Set steering/turning. 0 is straight, -1 is hard left, 1 is hard right. Clamped between -1 and 1. Default it 0. */
    public ActionSet withSteer(double steeringTilt) {
        this.steeringTilt = Math.max(-1, Math.min(1, steeringTilt));
        return this;
    }

    /** Set pitch. -1 for front flip, 1 for back flip. Clamped between -1 and 1. Default is 0. */
    public ActionSet withPitch(double pitchTilt) {
        this.pitchTilt = Math.max(-1, Math.min(1, pitchTilt));
        return this;
    }

    /** Set roll. -1 for left roll, 1 for right roll, Clamped between -1 and 1. Default is 0. */
    public ActionSet withRoll(double rollTilt) {
        this.rollTilt = Math.max(-1, Math.min(1, rollTilt));
        return this;
    }

    /** Set throttle. 0 is none, 1 is forwards, -1 is backwards. Clamped between -1 and 1. Default is 0. */
    public ActionSet withThrottle(double throttle) {
        this.throttle = Math.max(-1, Math.min(1, throttle));
        return this;
    }

    /** Set jump pressed output. Default is false. */
    public ActionSet withJump(boolean jumpDepressed) {
        this.jumpDepressed = jumpDepressed;
        return this;
    }

    /** Set boost pressed output. Default is false. */
    public ActionSet withBoost(boolean boostDepressed) {
        this.boostDepressed = boostDepressed;
        return this;
    }

    /** Set slide pressed output. Default is false. */
    public ActionSet withSlide(boolean slideDepressed) {
        this.slideDepressed = slideDepressed;
        return this;
    }

    /** Set jump pressed output to true. */
    public ActionSet withJump() {
        this.jumpDepressed = true;
        return this;
    }

    /** Set boost pressed output to true. */
    public ActionSet withBoost() {
        this.boostDepressed = true;
        return this;
    }

    /** Set slide pressed output to true. */
    public ActionSet withSlide() {
        this.slideDepressed = true;
        return this;
    }


    /**
     * Compare two Actionss.
     * @param o the other ActionSet.
     * @return whether the Actionss are identical.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActionSet that = (ActionSet) o;

        if (Double.compare(that.steeringTilt, steeringTilt) != 0) return false;
        if (Double.compare(that.pitchTilt, pitchTilt) != 0) return false;
        if (Double.compare(that.rollTilt, rollTilt) != 0) return false;
        if (Double.compare(that.throttle, throttle) != 0) return false;
        if (jumpDepressed != that.jumpDepressed) return false;
        if (boostDepressed != that.boostDepressed) return false;
        return slideDepressed == that.slideDepressed;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(steeringTilt);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(pitchTilt);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(rollTilt);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(throttle);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (jumpDepressed ? 1 : 0);
        result = 31 * result + (boostDepressed ? 1 : 0);
        result = 31 * result + (slideDepressed ? 1 : 0);
        return result;
    }

    /**
     * @return the amount of steering. Between -1 (left) and 1 (right).
     */
    public double getSteer() {
        return steeringTilt;
    }

    /**
     * @return the amount of pitch. Between -1 (forwards) and 1 (backwards).
     */
    public double getPitch() {
        return pitchTilt;
    }

    public double getRoll() {
        return rollTilt;
    }

    public double getThrottle() {
        return throttle;
    }

    public boolean isJumpDepressed() {
        return jumpDepressed;
    }

    public boolean isBoostDepressed() {
        return boostDepressed;
    }

    public boolean isSlideDepressed() {
        return slideDepressed;
    }

    public ControllerState toControllerState() {
        return new ControlsOutput()
                .withSteer((float) steeringTilt)
                .withYaw((float) steeringTilt)
                .withPitch((float) pitchTilt)
                .withRoll((float) rollTilt)
                .withThrottle((float) throttle)
                .withBoost(boostDepressed)
                .withSlide(slideDepressed)
                .withJump(jumpDepressed);
    }
}
