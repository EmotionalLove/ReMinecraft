package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityDestroyPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnMobPacket;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;

public class ServerEntityDestroyReaction implements IPacketReactor<ServerEntityDestroyPacket> {
    @Override
    public boolean takeAction(ServerEntityDestroyPacket pck) {
        for (int entityId : pck.getEntityIds()) {
            ReClient.ReClientCache.INSTANCE.entityCache.remove(entityId);
        }
        return true;
    }
}
