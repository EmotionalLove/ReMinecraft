package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityEquipmentPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.util.entity.Entity;
import com.sasha.reminecraft.util.entity.EntityEquipment;
import com.sasha.reminecraft.util.entity.EntityObject;

public class ServerEntityEquipmentReaction implements IPacketReactor<ServerEntityEquipmentPacket> {
    @Override
    public boolean takeAction(ServerEntityEquipmentPacket packet) {
        Entity entity = ReClient.ReClientCache.INSTANCE.entityCache.get(packet.getEntityId());
        if (entity instanceof EntityObject) {
            ReMinecraft.INSTANCE.terminalLogger.logError("Server tried adding equipment to an EntityObject! Ignoring.");
            return false;
        }
        EntityEquipment equipment = (EntityEquipment) entity;
        equipment.equipment.put(packet.getSlot(), packet.getItem());
        return true;
    }
}
