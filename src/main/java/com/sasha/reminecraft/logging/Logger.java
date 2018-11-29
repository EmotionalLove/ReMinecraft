package com.sasha.reminecraft.logging;

/**
 * Created by Sasha at 7:41 PM on 11/28/2018
 */
public interface Logger {

    void viewDebugs(boolean view);

    void log(String msg);

    void logWarning(String msg);

    void logError(String msg);

    void logDebug(String msg);

}
