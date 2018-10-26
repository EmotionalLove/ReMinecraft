package com.sasha.reminecraft.api.event;

import com.github.steveice10.packetlib.packet.Packet;
import com.sasha.eventsys.SimpleCancellableEvent;

/**
 * Invoked when we recieve a client-bound packet from the remote server
 */
public class RemoteServerPacketRecieveEvent extends SimpleCancellableEvent {

    private Packet pck;

    public RemoteServerPacketRecieveEvent(Packet pck) {
        this.pck = pck;
    }

    public Packet getRecievedPacket() {
        return pck;
    }

    public void setRecievedPacket(Packet pck) {
        this.pck = pck;
    }
}
