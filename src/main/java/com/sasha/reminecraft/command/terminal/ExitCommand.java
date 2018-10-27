package com.sasha.reminecraft.command.terminal;

import com.sasha.reminecraft.ReMinecraft;
import com.sasha.simplecmdsys.SimpleCommand;

/**
 * Quit RE:Minecraft
 */
public class ExitCommand extends SimpleCommand {
    public ExitCommand() {
        super("exit");
    }

    @Override
    public void onCommand() {
        ReMinecraft.INSTANCE.stop();
    }
}
