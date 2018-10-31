package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.github.steveice10.mc.protocol.data.game.world.notify.ClientNotification;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerNotifyClientPacket;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;

public class ServerNotifyClientReaction implements IPacketReactor<ServerNotifyClientPacket> {
    @Override
    public boolean takeAction(ServerNotifyClientPacket pck) {
        if (pck.getNotification() == ClientNotification.CHANGE_GAMEMODE) {
            ReClient.ReClientCache.INSTANCE.gameMode = (GameMode) pck.getValue();
        }
        return true;
    }
}
