package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.data.game.world.block.BlockChangeRecord;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerMultiBlockChangePacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.ChunkUtil;

public class ServerMultiBlockChangeReaction implements IPacketReactor<ServerMultiBlockChangePacket> {
    @Override
    public boolean takeAction(ServerMultiBlockChangePacket pck) {
        int chunkX = pck.getRecords()[0].getPosition().getX() >> 4;
        int chunkZ = pck.getRecords()[0].getPosition().getZ() >> 4;
        Column column = ReClient.ReClientCache.INSTANCE.chunkCache.getOrDefault(ChunkUtil.getChunkHashFromXZ(chunkX, chunkZ), null);
        if (column == null) {
            // not ignoring this can leak memory in the notchian client
            ReMinecraft.INSTANCE.logger.logDebug("Ignoring server request to change blocks in an unloaded chunk, is the remote server running a modified Minecraft server jar? This could cause issues.");
            return false;
        }
        for (BlockChangeRecord record : pck.getRecords()) {
            int relativeChunkX = Math.abs(Math.abs(record.getPosition().getX()) - (Math.abs(Math.abs(record.getPosition().getX() >> 4)) * 16));
            int relativeChunkZ = Math.abs(Math.abs(record.getPosition().getZ()) - (Math.abs(Math.abs(record.getPosition().getZ() >> 4)) * 16));
            int cubeY = ChunkUtil.clamp(record.getPosition().getY() >> 4, 0, 15);
            Chunk cube = column.getChunks()[cubeY];
            int cubeRelativeY = Math.abs(record.getPosition().getY() - 16 * cubeY);
            try {
                cube.getBlocks().set(relativeChunkX, ChunkUtil.clamp(cubeRelativeY, 0, 15), relativeChunkZ, record.getBlock());
                column.getChunks()[cubeY] = cube;
            } catch (Exception e) {
                System.out.println(relativeChunkX + " " + cubeRelativeY + " " + relativeChunkZ + " " + (cubeRelativeY << 8 | relativeChunkZ << 4 | relativeChunkX));
            }
        }
        ReClient.ReClientCache.INSTANCE.chunkCache.put(ChunkUtil.getChunkHashFromXZ(chunkX, chunkZ), column);
        return true;
    }
}
