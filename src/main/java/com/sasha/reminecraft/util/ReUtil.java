package com.sasha.reminecraft.util;

import java.io.File;
import java.io.IOException;

import static com.sasha.reminecraft.ReMinecraft.DATA_FILE;

public abstract class ReUtil {

    public static File getDataFile() {
        File file = new File(DATA_FILE + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static  File getDataFile(String s) {
        File file = new File(s + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

}
