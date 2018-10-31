package com.sasha.reminecraft.client;

import com.github.steveice10.packetlib.packet.Packet;

public interface IPacketReactor<T extends Packet> {

    /**
     * The action to take when a packet is recieved from the remote server
     * @param pck The packet
     * @return TRUE if RE:Minecraft should push the packet to the connected children,
     * FALSE if RE:Minecraft should BLOCK the packet from reaching the chilren.
     */
    boolean takeAction(T pck);

}
