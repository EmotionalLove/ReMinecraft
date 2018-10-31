package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityPropertiesPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.entity.EntityRotation;

public class ServerEntityPropertiesReaction implements IPacketReactor<ServerEntityPropertiesPacket> {
    @Override
    public boolean takeAction(ServerEntityPropertiesPacket pck) {
        EntityRotation rotation = (EntityRotation) ReClient.ReClientCache.INSTANCE.entityCache.get(pck.getEntityId());
        if (rotation == null) {
            ReMinecraft.INSTANCE.sendToChildren(pck);
            return false;
        }
        rotation.properties.addAll(pck.getAttributes());
        return true;
    }
}
