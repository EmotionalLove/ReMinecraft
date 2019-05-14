package com.sasha.reminecraft.api.event;

import com.sasha.eventsys.SimpleCancellableEvent;

import java.net.SocketAddress;

public class ChildDisconnectEvent extends SimpleCancellableEvent {

    private SocketAddress childIpAddress;

    public ChildDisconnectEvent(SocketAddress remoteAddress) {
        this.childIpAddress = remoteAddress;
    }

    public SocketAddress getChildIpAddress() {
        return childIpAddress;
    }
}
