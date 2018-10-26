package com.sasha.reminecraft.command.terminal;

import com.sasha.reminecraft.ReMinecraft;
import com.sasha.simplecmdsys.SimpleCommand;

/**
 * Quit RE:Minecraft
 */
public class RelaunchCommand extends SimpleCommand {
    public RelaunchCommand() {
        super("relaunch");
    }

    @Override
    public void onCommand() {
        ReMinecraft.INSTANCE.reLaunch();
    }
}
