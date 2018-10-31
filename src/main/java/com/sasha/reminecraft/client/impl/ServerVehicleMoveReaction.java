package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerVehicleMovePacket;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.entity.Entity;
import com.sasha.reminecraft.util.entity.EntityRotation;

public class ServerVehicleMoveReaction implements IPacketReactor<ServerVehicleMovePacket> {
    @Override
    public boolean takeAction(ServerVehicleMovePacket packet) {
        Entity entity = Entity.getEntityBeingRiddenBy(ReClient.ReClientCache.INSTANCE.entityId);
        if (entity == null) {
            return false;
        }
        entity.posX = packet.getX();
        entity.posY = packet.getY();
        entity.posZ = packet.getZ();
        if (entity instanceof EntityRotation) {
            ((EntityRotation) entity).yaw = packet.getYaw();
            ((EntityRotation) entity).pitch = packet.getPitch();
        }
        return true;
    }
}
