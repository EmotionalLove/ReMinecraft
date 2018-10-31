package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnObjectPacket;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.entity.EntityObject;
import com.sasha.reminecraft.util.entity.EntityType;

public class ServerSpawnObjectReaction implements IPacketReactor<ServerSpawnObjectPacket> {
    @Override
    public boolean takeAction(ServerSpawnObjectPacket pck) {
        EntityObject e = new EntityObject();
        e.type = EntityType.OBJECT;
        e.entityId = pck.getEntityId();
        e.uuid = pck.getUUID();
        e.objectType = pck.getType();
        e.posX = pck.getX();
        e.posY = pck.getY();
        e.posZ = pck.getZ();
        e.pitch = pck.getPitch();
        e.yaw = pck.getYaw();
        e.motionX = pck.getMotionX();
        e.motionY = pck.getMotionY();
        e.motionZ = pck.getMotionZ();
        e.data = pck.getData();
        ReClient.ReClientCache.INSTANCE.entityCache.put(e.entityId, e);
        return true;
    }
}
