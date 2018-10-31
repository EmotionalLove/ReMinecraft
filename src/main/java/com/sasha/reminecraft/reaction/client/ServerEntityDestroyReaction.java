package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityDestroyPacket;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;

public class ServerEntityDestroyReaction implements IPacketReactor<ServerEntityDestroyPacket> {
    @Override
    public boolean takeAction(ServerEntityDestroyPacket packet) {
        for (int entityId : packet.getEntityIds()) {
            ReClient.ReClientCache.INSTANCE.entityCache.remove(entityId);
        }
        return true;
    }
}
