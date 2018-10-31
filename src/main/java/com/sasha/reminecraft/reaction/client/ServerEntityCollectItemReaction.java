package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityCollectItemPacket;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;

public class ServerEntityCollectItemReaction implements IPacketReactor<ServerEntityCollectItemPacket> {
    @Override
    public boolean takeAction(ServerEntityCollectItemPacket packet) {
        ReClient.ReClientCache.INSTANCE.entityCache.remove(packet.getCollectedEntityId());
        return true;
    }
}
