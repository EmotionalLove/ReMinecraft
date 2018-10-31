package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntitySetPassengersPacket;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.entity.EntityEquipment;

public class ServerEntitySetPassengersReaction implements IPacketReactor<ServerEntitySetPassengersPacket> {
    @Override
    public boolean takeAction(ServerEntitySetPassengersPacket pck) {
        EntityEquipment equipment = (EntityEquipment) ReClient.ReClientCache.INSTANCE.entityCache.get(pck.getEntityId());
        if (pck.getPassengerIds() == null || pck.getPassengerIds().length == 0) {
            equipment.passengerIds = null;
        } else {
            equipment.passengerIds = pck.getPassengerIds();
        }
        return true;
    }
}
