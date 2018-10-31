package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.entity.EntityPlayer;
import com.sasha.reminecraft.util.entity.EntityType;

public class ServerSpawnPlayerReaction implements IPacketReactor<ServerSpawnPlayerPacket> {
    @Override
    public boolean takeAction(ServerSpawnPlayerPacket packet) {
        EntityPlayer e = new EntityPlayer();
        e.type = EntityType.PLAYER;
        e.entityId = packet.getEntityId();
        e.uuid = packet.getUUID();
        e.posX = packet.getX();
        e.posY = packet.getY();
        e.posZ = packet.getZ();
        e.pitch = packet.getPitch();
        e.yaw = packet.getYaw();
        e.metadata = packet.getMetadata();
        ReClient.ReClientCache.INSTANCE.entityCache.put(e.entityId, e);
        return true;
    }
}
