package com.sasha.reminecraft.api.event;

import com.github.steveice10.packetlib.packet.Packet;
import com.sasha.eventsys.SimpleCancellableEvent;
import com.sasha.reminecraft.client.children.ChildReClient;

/**
 * Invoked when we send a client-bound packet to a child client
 */
public class ChildServerPacketSendEvent extends SimpleCancellableEvent {

    private Packet pck;
    private ChildReClient child;

    public ChildServerPacketSendEvent(ChildReClient child, Packet pck) {
        this.pck = pck;
        this.child = child;
    }

    public Packet getSendingPacket() {
        return pck;
    }

    public void setSendingPacket(Packet pck) {
        this.pck = pck;
    }

    public ChildReClient getChild() {
        return child;
    }
}
