package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPlayerListDataPacket;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;

public class ServerPlayerListDataReaction implements IPacketReactor<ServerPlayerListDataPacket> {
    @Override
    public boolean takeAction(ServerPlayerListDataPacket pck) {
        ReClient.ReClientCache.INSTANCE.tabHeader = pck.getHeader();
        ReClient.ReClientCache.INSTANCE.tabFooter = pck.getFooter();
        return true;
    }
}
