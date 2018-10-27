package com.sasha.reminecraft.api.event;

import com.sasha.eventsys.SimpleEvent;

/**
 * Invoked when the server corrects our position
 */
public class ServerResetPlayerPositionEvent extends SimpleEvent {

    private double newPosX;
    private double newPosY;
    private double newPosZ;
    private float newYaw;
    private float newPitch;

    public ServerResetPlayerPositionEvent(double newPosX, double newPosY, double newPosZ,
                                          float newYaw, float newPitch) {
        this.newPosX = newPosX;
        this.newPosY = newPosY;
        this.newPosZ = newPosZ;
        this.newYaw = newYaw;
        this.newPitch = newPitch;
    }

    public double getNewPosX() {
        return newPosX;
    }

    public double getNewPosY() {
        return newPosY;
    }

    public double getNewPosZ() {
        return newPosZ;
    }

    public float getNewPitch() {
        return newPitch;
    }

    public float getNewYaw() {
        return newYaw;
    }
}
