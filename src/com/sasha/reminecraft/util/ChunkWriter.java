package com.sasha.reminecraft.util;

import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.sasha.reminecraft.Configuration;
import com.sasha.reminecraft.client.ReListener;

import java.io.*;

public class ChunkWriter {

    public static File dir() {
        File dir = new File(Configuration.var_remoteServerIp.replace(".", "_"));
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    private static void write(Column serialColumn, long hash) throws IOException {
        File file = new File(dir(), hash  + ".sck");
        if (!file.exists()) file.createNewFile();
        var fileOut = new FileOutputStream(file);
        var objOut = new ObjectOutputStream(fileOut);
        objOut.writeObject(serialColumn);
        objOut.close();
    }

    private static Column read(long hash) throws IOException, ClassNotFoundException {
        File file = new File(dir(), hash + ".sck");
        if (!file.exists()) return null;
        var fileIn = new FileInputStream(file);
        var objIn = new ObjectInputStream(fileIn);
        return (Column) objIn.readObject();
    }
    public static Column getChunk(long hash) throws IOException, ClassNotFoundException {
        return read(hash);
    }
    public static boolean hasChunk(long hash) {
        File file = new File(dir(), hash + ".sck");
        return file.exists();
    }
    public static void putChunk(Column column, long hash) throws IOException {
        write(column, hash);
        ReListener.ReListenerCache.chunkCache.remove(ChunkReference.getReferenceByHash(hash, ReListener.ReListenerCache.chunkCache));
        ReListener.ReListenerCache.chunkCache.add(new ChunkReference(hash, column.getTileEntities()));
    }
    public static void putChunk(long hash, Column column) throws IOException {
        write(column, hash);
        ReListener.ReListenerCache.chunkCache.remove(ChunkReference.getReferenceByHash(hash, ReListener.ReListenerCache.chunkCache));
        ReListener.ReListenerCache.chunkCache.add(new ChunkReference(hash, column.getTileEntities()));
    }
}
