package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPlayerListDataPacket;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.reaction.IPacketReactor;

public class ServerPlayerListDataReaction implements IPacketReactor<ServerPlayerListDataPacket> {
    @Override
    public boolean takeAction(ServerPlayerListDataPacket packet) {
        ReClient.ReClientCache.INSTANCE.tabHeader = packet.getHeader();
        ReClient.ReClientCache.INSTANCE.tabFooter = packet.getFooter();
        return true;
    }
}
