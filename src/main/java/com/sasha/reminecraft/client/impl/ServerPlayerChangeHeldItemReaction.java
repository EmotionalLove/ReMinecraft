package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerChangeHeldItemPacket;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;

public class ServerPlayerChangeHeldItemReaction implements IPacketReactor<ServerPlayerChangeHeldItemPacket> {
    @Override
    public boolean takeAction(ServerPlayerChangeHeldItemPacket packet) {
        ReClient.ReClientCache.INSTANCE.heldItem = packet.getSlot();
        return true;
    }
}
