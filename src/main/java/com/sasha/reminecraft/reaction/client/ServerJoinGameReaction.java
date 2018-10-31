package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.entity.EntityPlayer;
import com.sasha.reminecraft.util.entity.EntityType;

public class ServerJoinGameReaction implements IPacketReactor<ServerJoinGamePacket> {
    @Override
    public boolean takeAction(ServerJoinGamePacket packet) {
        ReClient.ReClientCache.INSTANCE.dimension = packet.getDimension();
        ReClient.ReClientCache.INSTANCE.entityId = packet.getEntityId();
        ReClient.ReClientCache.INSTANCE.gameMode = packet.getGameMode();
        EntityPlayer player = new EntityPlayer();
        player.type = EntityType.REAL_PLAYER;
        player.entityId = ReClient.ReClientCache.INSTANCE.entityId;
        player.uuid = ReClient.ReClientCache.INSTANCE.uuid;
        ReClient.ReClientCache.INSTANCE.player = player;
        ReClient.ReClientCache.INSTANCE.entityCache.put(player.entityId, player);
        return true;
    }
}
