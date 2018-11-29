package com.sasha.reminecraft.logging.impl;

import com.sasha.reminecraft.logging.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Sasha at 7:46 PM on 11/28/2018
 */
public class JavaFXLogger implements Logger {

    private final String name;
    private boolean seeDebug = false; //whether to display debug msgs

    public JavaFXLogger(String name) {
        this.name = name;
    }

    @Override
    public void viewDebugs(boolean view) {
        this.seeDebug = view;
    }

    @Override
    public void log(String msg) {
        System.out.println(msg);
    }

    @Override
    public void logWarning(String msg) {
        System.out.println(msg);
    }

    @Override
    public void logError(String msg) {
        System.out.println(msg);
    }

    @Override
    public void logDebug(String msg) {
        System.out.println(msg);
    }
}
