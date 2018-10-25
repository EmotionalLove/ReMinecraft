package com.sasha.reminecraft.util;

import com.sasha.reminecraft.util.serial.SerialColumn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class ChunkWriter {

    public static void write(SerialColumn serialColumn) throws IOException {
        File file = new File(serialColumn.getHash() + ".sck");
        if (!file.exists()) file.createNewFile();
        var fileOut = new FileOutputStream(file);
        var objOut = new ObjectOutputStream(fileOut);
    }

}
