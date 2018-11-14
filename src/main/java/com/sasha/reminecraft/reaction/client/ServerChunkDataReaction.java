package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerUnloadChunkPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.util.ChunkUtil;

public class ServerChunkDataReaction implements IPacketReactor<ServerChunkDataPacket> {
    @Override
    public boolean takeAction(ServerChunkDataPacket packet) {
        // VERY IMPORTANT: Chunks will NOT RENDER correctly and be invisible on notchian clients if we
        // do not actually push them correctly. This is apparent with big chunks and newly generated ones
        // that need to be dispersed over multiple packets. Trust me, it's really gay.
        // btw i love phi <33333333333333333 hes like a super nice bf
        Column column = packet.getColumn();
        long hash = ChunkUtil.getChunkHashFromXZ(column.getX(), column.getZ());
        if (!column.hasBiomeData()) {
            // if the chunk is thicc or newly generated
            if (ReClient.ReClientCache.INSTANCE.chunkCache.containsKey(hash)) {
                Column chunkToAddTo = ReClient.ReClientCache.INSTANCE.chunkCache.get(hash);
                ReMinecraft.INSTANCE.sendToChildren(new ServerUnloadChunkPacket(chunkToAddTo.getX(), chunkToAddTo.getZ()));
                for (int i = 0; i <= 15; i++) {
                    if (column.getChunks()[i] != null) {
                        chunkToAddTo.getChunks()[i] = column.getChunks()[i];
                    }
                }
                ReClient.ReClientCache.INSTANCE.chunkCache.put(hash, chunkToAddTo);
                ReMinecraft.INSTANCE.sendToChildren(new ServerChunkDataPacket(chunkToAddTo));
            }
        } else {
            ReClient.ReClientCache.INSTANCE.chunkCache.put(hash, packet.getColumn());
        }
        return true;
    }
}
