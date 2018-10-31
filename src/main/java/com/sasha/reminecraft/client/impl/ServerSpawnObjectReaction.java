package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnObjectPacket;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.entity.EntityObject;
import com.sasha.reminecraft.util.entity.EntityType;

public class ServerSpawnObjectReaction implements IPacketReactor<ServerSpawnObjectPacket> {
    @Override
    public boolean takeAction(ServerSpawnObjectPacket packet) {
        EntityObject e = new EntityObject();
        e.type = EntityType.OBJECT;
        e.entityId = packet.getEntityId();
        e.uuid = packet.getUUID();
        e.objectType = packet.getType();
        e.posX = packet.getX();
        e.posY = packet.getY();
        e.posZ = packet.getZ();
        e.pitch = packet.getPitch();
        e.yaw = packet.getYaw();
        e.motionX = packet.getMotionX();
        e.motionY = packet.getMotionY();
        e.motionZ = packet.getMotionZ();
        e.data = packet.getData();
        ReClient.ReClientCache.INSTANCE.entityCache.put(e.entityId, e);
        return true;
    }
}
