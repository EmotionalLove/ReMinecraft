package com.sasha.reminecraft.command.game;

import com.github.steveice10.mc.protocol.data.message.Message;
import com.sasha.simplecmdsys.SimpleCommand;
import com.sasha.simplecmdsys.SimpleCommandInfo;

@SimpleCommandInfo(description = "Return info about RE:Minecraft or a plugin",
syntax = {"", "<plugin>"})
public class AboutCommand extends SimpleCommand {

    public AboutCommand() {
        super("about");
    }

    @Override
    public void onCommand() {
        if (this.getArguments() == null || this.getArguments().length == 0) {
            Message line0 = Message.fromString("\247d\247lRE:Minecraft\247r\247d - Starting life on another server");
            Message line1 = Message.fromString("\2477https://github.com/EmotionalLove/ReMinecraft");
            Message line2 = Message.fromString("\2477Written by Sasha");
        }
    }
}
