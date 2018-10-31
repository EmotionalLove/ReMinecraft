package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityRemoveEffectPacket;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.entity.EntityEquipment;

public class ServerEntityRemoveEffectReaction implements IPacketReactor<ServerEntityRemoveEffectPacket> {
    @Override
    public boolean takeAction(ServerEntityRemoveEffectPacket pck) {
        EntityEquipment e = (EntityEquipment) ReClient.ReClientCache.INSTANCE.entityCache.get(pck.getEntityId());
        e.potionEffects.remove(pck.getEffect());
        return true;
    }
}
