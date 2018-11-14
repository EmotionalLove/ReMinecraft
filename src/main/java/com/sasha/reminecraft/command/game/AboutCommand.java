package com.sasha.reminecraft.command.game;

import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.RePluginLoader;
import com.sasha.reminecraft.util.TextMessageColoured;
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
            Message line0 = TextMessageColoured.from("&d&lRE:Minecraft &5" + ReMinecraft.VERSION + "&r&d - Starting life on another server");
            Message line1 = TextMessageColoured.from("&7https://github.com/EmotionalLove/ReMinecraft");
            Message line2 = TextMessageColoured.from("&7Written by Sasha");
            ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(line0));
            ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(line1));
            ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(line2));
            return;
        }
        if (this.getArguments().length != 1) {
            ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(TextMessageColoured.from("&4Invalid args.")));
        }
        RePluginLoader.getPluginList().forEach(pl -> {
            if (pl.pluginName.equalsIgnoreCase(this.getArguments()[0])) {
                Message line0 = TextMessageColoured.from("&7" + pl.pluginName);
                Message line1 = TextMessageColoured.from("&7" + pl.pluginVersion);
                Message line2 = TextMessageColoured.from("&7" + pl.pluginDescription);
                StringBuilder builder = new StringBuilder("&7");
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
                ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(TextMessageColoured.from(builder.toString())));
            }
        });
    }
}
