package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityHeadLookPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.entity.EntityRotation;

public class ServerEntityHeadLookReaction implements IPacketReactor<ServerEntityHeadLookPacket> {
    @Override
    public boolean takeAction(ServerEntityHeadLookPacket pck) {
        EntityRotation e = (EntityRotation) ReClient.ReClientCache.INSTANCE.entityCache.get(pck.getEntityId());
        if (e == null) {
            ReMinecraft.INSTANCE.logger.logDebug
                    ("Null entity with entity id " + pck.getEntityId());
            ReMinecraft.INSTANCE.sendToChildren(pck);
            return false;
        }
        e.headYaw = pck.getHeadYaw();
        return true;
    }
}
