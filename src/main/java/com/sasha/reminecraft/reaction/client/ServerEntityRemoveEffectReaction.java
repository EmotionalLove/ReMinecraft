package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityRemoveEffectPacket;
import com.sasha.reminecraft.reaction.IPacketReactor;
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
