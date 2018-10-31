package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityTeleportPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.client.children.ChildReClient;
import com.sasha.reminecraft.util.entity.Entity;
import com.sasha.reminecraft.util.entity.EntityRotation;

public class ServerEntityTeleportReaction implements IPacketReactor<ServerEntityTeleportPacket> {
    @Override
    public boolean takeAction(ServerEntityTeleportPacket pck) {
        Entity entity = ReClient.ReClientCache.INSTANCE.entityCache.get(pck.getEntityId());
        if (entity == null) {
            ReMinecraft.INSTANCE.logger.logDebug
                    ("Null entity with entity id " + pck.getEntityId());
            ReMinecraft.INSTANCE.childClients.stream()
                    .filter(ChildReClient::isPlaying)
                    .forEach(client -> client.getSession().send(pck));
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
