package com.sasha.reminecraft.reaction.server;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientKeepAlivePacket;
import com.sasha.reminecraft.reaction.IPacketReactor;

public class ClientKeepAliveReaction implements IPacketReactor<ClientKeepAlivePacket> {


    @Override
    public boolean takeAction(ClientKeepAlivePacket packet) {
        return false;
    }
}
