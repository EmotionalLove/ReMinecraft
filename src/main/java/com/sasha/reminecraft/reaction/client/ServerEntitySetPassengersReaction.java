package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntitySetPassengersPacket;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.util.entity.Entity;
import com.sasha.reminecraft.util.entity.EntityEquipment;

public class ServerEntitySetPassengersReaction implements IPacketReactor<ServerEntitySetPassengersPacket> {
    @Override
    public boolean takeAction(ServerEntitySetPassengersPacket packet) {
        Entity equipment = ReClient.ReClientCache.INSTANCE.entityCache.get(packet.getEntityId());
        if (packet.getPassengerIds().length == 0) {
            equipment.clearEntity();
        } else {
            equipment.setEntity(packet.getPassengerIds());
        }
        return true;
    }
}
