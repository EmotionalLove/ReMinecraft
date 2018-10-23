package com.sasha.reminecraft.command;

import com.sasha.reminecraft.ReMinecraft;
import com.sasha.simplecmdsys.SimpleCommand;

public class ExitCommand extends SimpleCommand {
    public ExitCommand() {
        super("exit");
    }

    @Override
    public void onCommand() {
        ReMinecraft.INSTANCE.stop();
    }
}
