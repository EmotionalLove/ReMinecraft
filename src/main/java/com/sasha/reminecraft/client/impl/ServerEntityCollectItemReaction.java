package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityCollectItemPacket;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;

public class ServerEntityCollectItemReaction implements IPacketReactor<ServerEntityCollectItemPacket> {
    @Override
    public boolean takeAction(ServerEntityCollectItemPacket pck) {
        ReClient.ReClientCache.INSTANCE.entityCache.remove(pck.getCollectedEntityId());
        return true;
    }
}
