package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityHeadLookPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.util.entity.EntityRotation;

public class ServerEntityHeadLookReaction implements IPacketReactor<ServerEntityHeadLookPacket> {
    @Override
    public boolean takeAction(ServerEntityHeadLookPacket packet) {
        EntityRotation e = (EntityRotation) ReClient.ReClientCache.INSTANCE.entityCache.get(packet.getEntityId());
        if (e == null) {
            ReMinecraft.LOGGER.logDebug
                    ("Null entity with entity id " + packet.getEntityId());
            ReMinecraft.INSTANCE.sendToChildren(packet);
            return false;
        }
        e.headYaw = packet.getHeadYaw();
        return true;
    }
}
