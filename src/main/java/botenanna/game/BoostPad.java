package botenanna.game;

import botenanna.math.Vector3;
import rlbot.flat.BoostPadState;

public class BoostPad {

    public static final int PAD_RADIUS = 165;
    public static final int COUNT_BIG_PADS = 6;
    public static final int COUNT_SMALL_PADS = 28;
    public static final int COUNT_TOTAL_PADS = COUNT_BIG_PADS + COUNT_SMALL_PADS;
    public static final int AMOUNT_SMALL = 12;
    public static final int AMOUNT_BIG = 100;
    public static final int RESPAWN_TIME_BIG = 10;
    public static final int RESPAWN_TIME_SMALL = 3;

    private Vector3 position;
    private double respawnTimeLeft;
    private boolean isBigBoostPad;

    public BoostPad(BoostPad from) {
        position = from.getPosition();
        setRespawnTimeLeft(respawnTimeLeft);
        isBigBoostPad = from.isBigBoostPad;
    }

    public BoostPad(rlbot.flat.BoostPad boostPad, BoostPadState state) {
        position = Vector3.convert(boostPad.location()).withZ(0);
        setRespawnTimeLeft(state.timer()); // TODO BoostPadState timer does not respawn timer
        isBigBoostPad = boostPad.isFullBoost();
    }

    @Override
    public String toString() {
        return "BoostPad(x: " + position.x + ", y: " + position.y +
                ". t: " + respawnTimeLeft + ", big: " + isBigBoostPad + ")";
    }

    public Vector3 getPosition() {
        return position;
    }

    public Boolean isActive() {
        return respawnTimeLeft <= 0;
    }

    public double getRespawnTimeLeft() {
        return respawnTimeLeft;
    }

    public void setRespawnTimeLeft(double respawnTimeLeft) {
        this.respawnTimeLeft = Math.max(0, respawnTimeLeft);
    }

    /** @return whether the BoostPad is active after the reduction. */
    public boolean reduceRespawnTimeLeft(double amount) {
        setRespawnTimeLeft(respawnTimeLeft - amount);
        return isActive();
    }

    public void refreshRespawnTimer() {
        respawnTimeLeft = isBigBoostPad ? RESPAWN_TIME_BIG : RESPAWN_TIME_SMALL;
    }

    public void setActive() {
        respawnTimeLeft = 0;
    }

    public int getBoostAmount() {
        return isBigBoostPad ? AMOUNT_BIG : AMOUNT_SMALL;
    }
}
