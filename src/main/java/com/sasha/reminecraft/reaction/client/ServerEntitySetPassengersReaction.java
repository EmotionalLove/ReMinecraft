package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntitySetPassengersPacket;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.entity.EntityEquipment;

public class ServerEntitySetPassengersReaction implements IPacketReactor<ServerEntitySetPassengersPacket> {
    @Override
    public boolean takeAction(ServerEntitySetPassengersPacket packet) {
        EntityEquipment equipment = (EntityEquipment) ReClient.ReClientCache.INSTANCE.entityCache.get(packet.getEntityId());
        if (packet.getPassengerIds() == null || packet.getPassengerIds().length == 0) {
            equipment.passengerIds = null;
        } else {
            equipment.passengerIds = packet.getPassengerIds();
        }
        return true;
    }
}
