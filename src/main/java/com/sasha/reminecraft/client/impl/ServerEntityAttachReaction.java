package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityAttachPacket;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.entity.EntityRotation;

public class ServerEntityAttachReaction implements IPacketReactor<ServerEntityAttachPacket> {
    @Override
    public boolean takeAction(ServerEntityAttachPacket pck) {
        EntityRotation entityRotation = (EntityRotation) ReClient.ReClientCache.INSTANCE.entityCache.get(pck.getEntityId());
        if (pck.getAttachedToId() == -1) {
            entityRotation.isLeashed = false;
            entityRotation.leashedID = pck.getAttachedToId();
        } else {
            entityRotation.isLeashed = true;
            entityRotation.leashedID = pck.getAttachedToId();
        }
        return true;
    }
}
