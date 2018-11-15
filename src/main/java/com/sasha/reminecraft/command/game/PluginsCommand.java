package com.sasha.reminecraft.command.game;

import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.RePluginLoader;
import com.sasha.reminecraft.util.TextMessageColoured;
import com.sasha.simplecmdsys.SimpleCommand;
import com.sasha.simplecmdsys.SimpleCommandInfo;

import java.util.concurrent.atomic.AtomicInteger;

@SimpleCommandInfo(description = "Return a list of loaded plugins", syntax = {""})
public class PluginsCommand extends SimpleCommand {

    public PluginsCommand() {
        super("plugins");
    }

    @Override
    public void onCommand() {
        if (RePluginLoader.getPluginList().isEmpty()) {
            ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(TextMessageColoured.from("&4There are no plugins loaded.")));
            return;
        }
        StringBuilder builder = new StringBuilder("&7");
        AtomicInteger c = new AtomicInteger();
        RePluginLoader.getPluginList().forEach(pl -> {
            if (c.get() == 0) {
                builder.append(pl.pluginName);
                c.getAndIncrement();
                return;
            }
            builder.append(", ").append(pl.pluginName);
            c.getAndIncrement();
        });

        int i = c.get();
        ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(TextMessageColoured.from(builder.toString())));
        ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(TextMessageColoured.from("&e" + c.get() + " plugin" + (i == 1 ? "" : "s") + " loaded")));
    }
}
