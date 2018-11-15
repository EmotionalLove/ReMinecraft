package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityPropertiesPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.util.entity.EntityRotation;

public class ServerEntityPropertiesReaction implements IPacketReactor<ServerEntityPropertiesPacket> {
    @Override
    public boolean takeAction(ServerEntityPropertiesPacket packet) {
        EntityRotation rotation = (EntityRotation) ReClient.ReClientCache.INSTANCE.entityCache.get(packet.getEntityId());
        if (rotation == null) {
            ReMinecraft.INSTANCE.sendToChildren(packet);
            return false;
        }
        rotation.properties.addAll(packet.getAttributes());
        return true;
    }
}
