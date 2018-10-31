package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.ingame.server.ServerRespawnPacket;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;

public class ServerRespawnReaction implements IPacketReactor<ServerRespawnPacket> {
    @Override
    public boolean takeAction(ServerRespawnPacket packet) {
        ReClient.ReClientCache.INSTANCE.dimension = packet.getDimension();
        ReClient.ReClientCache.INSTANCE.gameMode = packet.getGameMode();
        ReClient.ReClientCache.INSTANCE.chunkCache.clear();
        ReClient.ReClientCache.INSTANCE.entityCache.entrySet().removeIf(integerEntityEntry -> integerEntityEntry.getKey() != ReClient.ReClientCache.INSTANCE.entityId);
        ReClient.ReClientCache.INSTANCE.cachedBossBars.clear();
        ReClient.ReClientCache.INSTANCE.player.potionEffects.clear();
        return true;
    }
}
