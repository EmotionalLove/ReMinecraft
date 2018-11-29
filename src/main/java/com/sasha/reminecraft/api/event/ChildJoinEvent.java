package com.sasha.reminecraft.api.event;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.sasha.eventsys.SimpleCancellableEvent;

public class ChildJoinEvent extends SimpleCancellableEvent {

    private String cancelledKickMessage;
    private GameProfile profile;

    public ChildJoinEvent(GameProfile profile) {
        this.profile = profile;
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    public void setCancelledKickMessage(String msg) {
        this.cancelledKickMessage = msg;
    }

    public String getCancelledKickMessage() {
        return cancelledKickMessage;
    }
}
