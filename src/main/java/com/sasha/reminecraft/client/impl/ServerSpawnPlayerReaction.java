package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnMobPacket;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.entity.EntityPlayer;
import com.sasha.reminecraft.util.entity.EntityType;

public class ServerSpawnPlayerReaction implements IPacketReactor<ServerSpawnMobPacket> {
    @Override
    public boolean takeAction(ServerSpawnMobPacket pck) {
        EntityPlayer e = new EntityPlayer();
        e.type = EntityType.PLAYER;
        e.entityId = pck.getEntityId();
        e.uuid = pck.getUUID();
        e.posX = pck.getX();
        e.posY = pck.getY();
        e.posZ = pck.getZ();
        e.pitch = pck.getPitch();
        e.yaw = pck.getYaw();
        e.headYaw = pck.getHeadYaw();
        e.motionX = pck.getMotionX();
        e.motionY = pck.getMotionY();
        e.motionZ = pck.getMotionZ();
        e.metadata = pck.getMetadata();
        ReClient.ReClientCache.INSTANCE.entityCache.put(e.entityId, e);
        return true;
    }
}
