package com.sasha.reminecraft;

/**
 * A simple logging mechanism
 */
public class Logger {
    private final String name;
    private boolean seeDebug = false; //whether to display debug msgs

    public Logger(String name) {
        this.name = name;
    }

    public void viewDebugs(boolean view) {
        seeDebug = view;
    }

    public void log(String msg) {
        System.out.println("[" + name + " / INFO] " + msg);
    }

    public void logWarning(String msg) {
        System.out.println("[" + name + " / WARN] " + msg);
    }

    public void logError(String msg) {
        System.out.println("[" + name + " / ERROR] " + msg);
    }

    public void logDebug(String msg) {
        if (seeDebug) System.out.println("[" + name + " / DEBUG] " + msg);
    }

}
