package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerVehicleMovePacket;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.entity.Entity;
import com.sasha.reminecraft.util.entity.EntityRotation;

public class ServerVehicleMoveReaction implements IPacketReactor<ServerVehicleMovePacket> {
    @Override
    public boolean takeAction(ServerVehicleMovePacket pck) {
        Entity entity = Entity.getEntityBeingRiddenBy(ReClient.ReClientCache.INSTANCE.entityId);
        if (entity == null) {
            return false;
        }
        entity.posX = pck.getX();
        entity.posY = pck.getY();
        entity.posZ = pck.getZ();
        if (entity instanceof EntityRotation) {
            ((EntityRotation) entity).yaw = pck.getYaw();
            ((EntityRotation) entity).pitch = pck.getPitch();
        }
        return true;
    }
}
