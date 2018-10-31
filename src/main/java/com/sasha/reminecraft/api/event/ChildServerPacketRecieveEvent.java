package com.sasha.reminecraft.api.event;

import com.github.steveice10.packetlib.packet.Packet;
import com.sasha.eventsys.SimpleCancellableEvent;
import com.sasha.reminecraft.client.ChildReClient;

/**
 * Invoked when we recieve a server-bound packet from the child server
 */
public class ChildServerPacketRecieveEvent extends SimpleCancellableEvent {

    private Packet pck;
    private ChildReClient child;

    public ChildServerPacketRecieveEvent(ChildReClient child, Packet pck) {
        this.pck = pck;
        this.child = child;
    }

    public Packet getRecievedPacket() {
        return pck;
    }

    public void setRecievedPacket(Packet pck) {
        this.pck = pck;
    }

    public ChildReClient getChild() {
        return child;
    }
}
