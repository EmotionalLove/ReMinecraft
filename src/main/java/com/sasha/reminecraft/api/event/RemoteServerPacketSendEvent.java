package com.sasha.reminecraft.api.event;

import com.github.steveice10.packetlib.packet.Packet;
import com.sasha.eventsys.SimpleCancellableEvent;

/**
 * Invoked when we send a server-bound packet to the remote server
 */
public class RemoteServerPacketSendEvent extends SimpleCancellableEvent {

    private Packet pck;

    public RemoteServerPacketSendEvent(Packet pck) {
        this.pck = pck;
    }

    public Packet getSendingPacket() {
        return pck;
    }

    public void getSendingPacket(Packet pck) {
        this.pck = pck;
    }
}
