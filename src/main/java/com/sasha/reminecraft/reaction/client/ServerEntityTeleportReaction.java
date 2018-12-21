package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityTeleportPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.client.ChildReClient;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.util.entity.Entity;
import com.sasha.reminecraft.util.entity.EntityRotation;

public class ServerEntityTeleportReaction implements IPacketReactor<ServerEntityTeleportPacket> {
    @Override
    public boolean takeAction(ServerEntityTeleportPacket packet) {
        Entity entity = ReClient.ReClientCache.INSTANCE.entityCache.get(packet.getEntityId());
        if (entity == null) {
            ReMinecraft.LOGGER.logDebug
                    ("Null entity with entity id " + packet.getEntityId());
            ReMinecraft.INSTANCE.childClients.stream()
                    .filter(ChildReClient::isPlaying)
                    .forEach(client -> client.getSession().send(packet));
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
