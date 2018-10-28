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
        if (ReMinecraft.INSTANCE.minecraftClient != null) {
            ReMinecraft.INSTANCE.minecraftClient.getSession().disconnect("Relaunching RE:Minecraft");
        }
    }
}
