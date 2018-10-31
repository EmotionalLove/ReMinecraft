package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.github.steveice10.mc.protocol.data.game.world.notify.ClientNotification;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerNotifyClientPacket;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;

public class ServerNotifyClientReaction implements IPacketReactor<ServerNotifyClientPacket> {
    @Override
    public boolean takeAction(ServerNotifyClientPacket packet) {
        if (packet.getNotification() == ClientNotification.CHANGE_GAMEMODE) {
            ReClient.ReClientCache.INSTANCE.gameMode = (GameMode) packet.getValue();
        }
        return true;
    }
}
