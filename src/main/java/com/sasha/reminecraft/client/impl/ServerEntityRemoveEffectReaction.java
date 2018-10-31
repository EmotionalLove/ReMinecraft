package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityRemoveEffectPacket;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.entity.EntityEquipment;

public class ServerEntityRemoveEffectReaction implements IPacketReactor<ServerEntityRemoveEffectPacket> {
    @Override
    public boolean takeAction(ServerEntityRemoveEffectPacket packet) {
        EntityEquipment e = (EntityEquipment) ReClient.ReClientCache.INSTANCE.entityCache.get(packet.getEntityId());
        e.potionEffects.remove(packet.getEffect());
        return true;
    }
}
