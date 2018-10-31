package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerWindowItemsPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerUnloadChunkPacket;
import com.sasha.reminecraft.client.IPacketReactor;
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
