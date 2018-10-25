package com.sasha.reminecraft.util;

import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.sasha.reminecraft.Configuration;
import com.sasha.reminecraft.client.ReListener;
import com.sasha.reminecraft.server.ReAdapter;

import java.io.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChunkWriter {

    private static Lock lck = new ReentrantLock();

    public static File dir() {
        File dir = new File(Configuration.var_remoteServerIp.replace(".", "_"));
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    private static void write(Column serialColumn, long hash) throws IOException {
        lck.lock();
        try {
            File file = new File(dir(), hash + ".sck");
            if (!file.exists()) file.createNewFile();
            var fileOut = new FileOutputStream(file);
            var objOut = new ObjectOutputStream(fileOut);
            objOut.writeObject(serialColumn);
            objOut.close();
        }finally {
            lck.unlock();
        }
    }

    private static Column read(long hash) throws IOException, ClassNotFoundException {
        lck.lock();
        try {
            File file = new File(dir(), hash + ".sck");
            if (!file.exists()) return null;
            var fileIn = new FileInputStream(file);
            var objIn = new ObjectInputStream(fileIn);
            return (Column) objIn.readObject();
        } finally {
            lck.unlock();
        }
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
        ReAdapter.joinLock.lock();
        try {
            ReListener.ReListenerCache.chunkCache.remove(ChunkReference.getReferenceByHash(hash));
            ReListener.ReListenerCache.chunkCache.add(new ChunkReference(hash, column.getTileEntities()));
        } finally {
            ReAdapter.joinLock.unlock();
        }
    }
    public static void putChunk(long hash, Column column) throws IOException {
        write(column, hash);
        ReAdapter.joinLock.lock();
        try {
            ReListener.ReListenerCache.chunkCache.remove(ChunkReference.getReferenceByHash(hash));
            ReListener.ReListenerCache.chunkCache.add(new ChunkReference(hash, column.getTileEntities()));
        } finally {
            ReAdapter.joinLock.unlock();
        }
    }
}
