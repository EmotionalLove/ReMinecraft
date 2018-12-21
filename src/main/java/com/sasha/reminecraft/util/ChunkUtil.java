package com.sasha.reminecraft.util;

public class ChunkUtil {

    public int x;
    public int z;
    public long hash;

    public ChunkUtil(long hash) {
        this.hash = hash;
        this.x = getXFromHash(hash);
        this.z = getZFromHash(hash);
    }

    public ChunkUtil(int x, int z) {
        this.x = x;
        this.z = z;
        this.hash = getChunkHashFromXZ(x, z);
    }

    public static long getChunkHashFromXZ(int x, int z) {
        return (((long) x) << 32) | (z & 0xffffffffL);
    }

    public static ChunkUtil getPositionFromHash(long hash) {
        return new ChunkUtil(getXFromHash(hash), getZFromHash(hash));
    }

    public static int getXFromHash(long hash) {
        return (int) (hash >> 32);
    }

    public static int getZFromHash(long hash) {
        return (int) hash;
    }

    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }
}