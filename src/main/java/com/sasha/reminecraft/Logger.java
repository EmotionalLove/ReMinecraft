package com.sasha.reminecraft;

import org.jline.reader.LineReader;
import org.jline.utils.InfoCmp;

import java.io.Console;

import static com.sasha.reminecraft.ReMinecraft.reader;

/**
 * A simple logging mechanism
 */
public class Logger {

    private Console stashed;

    private final String name;
    private boolean seeDebug = false; //whether to display debug msgs

    public Logger(String name) {
        this.name = name;
    }

    public void viewDebugs(boolean view) {
        seeDebug = view;
    }

    public void log(String msg) {
        this.println("[" + name + " / INFO] " + msg);

    }

    public void logWarning(String msg) {
        this.println("[" + name + " / WARN] " + msg);
    }

    public void logError(String msg) {
        this.println("[" + name + " / ERROR] " + msg);
    }

    public void logDebug(String msg) {
        if (seeDebug) this.println("[" + name + " / DEBUG] " + msg);
    }

    private void println(String msg) {
        reader.getTerminal().puts(InfoCmp.Capability.carriage_return);
        reader.getTerminal().writer().println(msg);
        try {
            reader.callWidget(LineReader.REDRAW_LINE);
            reader.callWidget(LineReader.REDISPLAY);
        }catch (Exception ignored){}
        reader.getTerminal().writer().flush();
    }

}
