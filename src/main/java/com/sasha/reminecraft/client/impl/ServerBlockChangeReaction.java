package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerBlockChangePacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.ChunkUtil;

public class ServerBlockChangeReaction implements IPacketReactor<ServerBlockChangePacket> {
    @Override
    public boolean takeAction(ServerBlockChangePacket pck) {
        int chunkX = pck.getRecord().getPosition().getX() >> 4;
        int chunkZ = pck.getRecord().getPosition().getZ() >> 4;
        int cubeY = ChunkUtil.clamp(pck.getRecord().getPosition().getY() >> 4, 0, 15);
        Column column = ReClient.ReClientCache.INSTANCE.chunkCache
                .getOrDefault(ChunkUtil.getChunkHashFromXZ(chunkX, chunkZ), null);
        if (column == null) {
            // not ignoring this can leak memory in the notchian client
            ReMinecraft.INSTANCE.logger.logDebug("Ignoring server request to change blocks in an unloaded chunk, is the remote server running a modified Minecraft server jar? This could cause issues.");
            return false;
        }
        Chunk subChunk = column.getChunks()[cubeY];
        int cubeRelY = Math.abs(pck.getRecord().getPosition().getY() - 16 * cubeY);
        try {
            subChunk.getBlocks().set(Math.abs(Math.abs(pck.getRecord().getPosition().getX()) - (Math.abs(Math.abs(pck.getRecord().getPosition().getX() >> 4)) * 16)), ChunkUtil.clamp(cubeRelY, 0, 15), Math.abs(Math.abs(pck.getRecord().getPosition().getZ()) - (Math.abs(Math.abs(pck.getRecord().getPosition().getZ() >> 4)) * 16)), pck.getRecord().getBlock());
            column.getChunks()[cubeY] = subChunk;
            ReClient.ReClientCache.INSTANCE.chunkCache.put(ChunkUtil.getChunkHashFromXZ(chunkX, chunkZ), column);
        } catch (Exception e) {
            //
        }
        ReClient.ReClientCache.INSTANCE.chunkCache.put(ChunkUtil.getChunkHashFromXZ(chunkX, chunkZ), column);
        return true;
    }
}
