package com.sasha.reminecraft.api.event;

import com.sasha.eventsys.SimpleEvent;

/**
 * Invoked when the player gets hurt
 */
public class PlayerDamagedEvent extends SimpleEvent {

    private float oldHealth;
    private float newHealth;

    public PlayerDamagedEvent(float oldHealth, float newHealth) {
        this.oldHealth = oldHealth;
        this.newHealth = newHealth;
    }

    public float getNewHealth() {
        return newHealth;
    }

    public float getOldHealth() {
        return oldHealth;
    }
}
