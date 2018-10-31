package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerWindowItemsPacket;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;

public class ServerWindowItemsReaction implements IPacketReactor<ServerWindowItemsPacket> {
    @Override
    public boolean takeAction(ServerWindowItemsPacket packet) {
        if (packet.getWindowId() == 0) {
            ReClient.ReClientCache.INSTANCE.playerInventory = packet.getItems();
        }
        return true;
    }
}
