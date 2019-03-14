package com.sasha.reminecraft.api.event;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.sasha.eventsys.SimpleCancellableEvent;

import java.net.SocketAddress;

public class ChildJoinEvent extends SimpleCancellableEvent {

    private String cancelledKickMessage;
    private SocketAddress childIpAddress;
    private GameProfile profile;

    public ChildJoinEvent(GameProfile profile, SocketAddress remoteAddress) {
        this.profile = profile;
        this.childIpAddress = remoteAddress;
    }

    public SocketAddress getChildIpAddress() {
        return childIpAddress;
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
