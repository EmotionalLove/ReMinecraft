package com.sasha.reminecraft.command.game;

import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.RePluginLoader;
import com.sasha.simplecmdsys.SimpleCommand;
import com.sasha.simplecmdsys.SimpleCommandInfo;

import java.util.concurrent.atomic.AtomicInteger;

@SimpleCommandInfo(description = "Return info about RE:Minecraft or a plugin",
        syntax = {"", "<plugin>"})
public class AboutCommand extends SimpleCommand {

    public AboutCommand() {
        super("about");
    }

    @Override
    public void onCommand() {
        if (this.getArguments() == null || this.getArguments().length == 0) {
            Message line0 = Message.fromString("\247d\247lRE:Minecraft \2475{}\247r\247d - Starting life on another server".replace("{}", ReMinecraft.VERSION));
            Message line1 = Message.fromString("\2477https://github.com/EmotionalLove/ReMinecraft");
            Message line2 = Message.fromString("\2477Written by Sasha");
            ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(line0));
            ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(line1));
            ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(line2));
            return;
        }
        if (this.getArguments().length != 1) {
            ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(Message.fromString("\2474Invalid args.")));
        }
        RePluginLoader.getPluginList().forEach(pl -> {
            if (pl.pluginName.equalsIgnoreCase(this.getArguments()[0])) {
                Message line0 = Message.fromString("\2477" + pl.pluginName);
                Message line1 = Message.fromString("\2477" + pl.pluginVersion);
                Message line2 = Message.fromString("\2477" + pl.pluginDescription);
                StringBuilder builder = new StringBuilder("\2477");
                AtomicInteger c = new AtomicInteger();
                for (String pluginAuthor : pl.pluginAuthors) {
                    if (c.get() == 0) {
                        builder.append(pluginAuthor);
                        return;
                    }
                    builder.append(", ").append(pluginAuthor);
                    c.getAndIncrement();
                }
                ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(line0));
                ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(line1));
                ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(line2));
                ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(Message.fromString(builder.toString())));
            }
        });
    }
}
