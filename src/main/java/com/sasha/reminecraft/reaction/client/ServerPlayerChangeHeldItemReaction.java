package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerChangeHeldItemPacket;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;

public class ServerPlayerChangeHeldItemReaction implements IPacketReactor<ServerPlayerChangeHeldItemPacket> {
    @Override
    public boolean takeAction(ServerPlayerChangeHeldItemPacket packet) {
        ReClient.ReClientCache.INSTANCE.heldItem = packet.getSlot();
        return true;
    }
}
