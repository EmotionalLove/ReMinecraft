package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerUnloadChunkPacket;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.ChunkUtil;

public class ServerUnloadChunkReaction implements IPacketReactor<ServerUnloadChunkPacket> {
    @Override
    public boolean takeAction(ServerUnloadChunkPacket packet) {
        long hash = ChunkUtil.getChunkHashFromXZ(packet.getX(), packet.getZ());
        ReClient.ReClientCache.INSTANCE.chunkCache.remove(hash);
        return true;
    }
}
