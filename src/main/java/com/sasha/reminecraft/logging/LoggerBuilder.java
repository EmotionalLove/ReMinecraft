package com.sasha.reminecraft.logging;

import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.logging.impl.JavaFXLogger;
import com.sasha.reminecraft.logging.impl.TerminalLogger;

/**
 * Created by Sasha at 8:17 PM on 12/1/2018
 */
public abstract class LoggerBuilder {

    public static ILogger buildProperLogger(String name) {
        if (ReMinecraft.isUsingJavaFXGui) {
            return new JavaFXLogger(name);
        }
        return new TerminalLogger(name);
    }
}
