package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityAttachPacket;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.entity.EntityRotation;

public class ServerEntityAttachReaction implements IPacketReactor<ServerEntityAttachPacket> {
    @Override
    public boolean takeAction(ServerEntityAttachPacket packet) {
        EntityRotation entityRotation = (EntityRotation) ReClient.ReClientCache.INSTANCE.entityCache.get(packet.getEntityId());
        if (packet.getAttachedToId() == -1) {
            entityRotation.isLeashed = false;
            entityRotation.leashedID = packet.getAttachedToId();
        } else {
            entityRotation.isLeashed = true;
            entityRotation.leashedID = packet.getAttachedToId();
        }
        return true;
    }
}
