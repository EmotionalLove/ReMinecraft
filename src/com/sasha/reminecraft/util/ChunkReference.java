package com.sasha.reminecraft.util;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.sasha.reminecraft.client.ReListener;

import java.util.List;

public class ChunkReference {

    private long hash;
    private CompoundTag[] tag;

    public ChunkReference(long hash, CompoundTag[] tag) {
        this.hash = hash;
        this.tag = tag;
    }

    public CompoundTag[] getTag() {
        return tag;
    }

    public long getHash() {
        return hash;
    }

    public void setTag(CompoundTag[] tag) {
        this.tag = tag;
    }

    public void setHash(long hash) {
        this.hash = hash;
    }

    public static ChunkReference getReferenceByHash(long hash) {
        var list = ReListener.ReListenerCache.chunkCache;
        for (ChunkReference chunkRef : list) {
            var chunkHash = chunkRef.getHash();
            if (chunkHash == hash) {
                return chunkRef;
            }
        }
        return null;
    }
    public static ChunkReference getReferanceByTileData(CompoundTag[] tag) {
        var list = ReListener.ReListenerCache.chunkCache;
        for (ChunkReference chunkRef : list) {
            var chunkTag = chunkRef.tag;
            if (chunkTag == tag) {
                return chunkRef;
            }
        }
        return null;
    }
}
