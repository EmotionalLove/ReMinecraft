package com.sasha.reminecraft.command.game;

import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.RePluginLoader;
import com.sasha.simplecmdsys.SimpleCommand;
import com.sasha.simplecmdsys.SimpleCommandInfo;

import java.util.concurrent.atomic.AtomicInteger;

@SimpleCommandInfo(description = "Return a list of loaded plugins", syntax = {})
public class PluginsCommand extends SimpleCommand {

    public PluginsCommand() {
        super("plugins");
    }

    @Override
    public void onCommand() {
        if (RePluginLoader.getPluginList().isEmpty()) {
            ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(Message.fromString("\2474There are no plugins loaded.")));
            return;
        }
        StringBuilder builder = new StringBuilder("\2477");
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
        ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(Message.fromString(builder.toString())));
        ReMinecraft.INSTANCE.sendToChildren(
                new ServerChatPacket(
                        Message.fromString(
                                c.get() + " plugin$s loaded".replace("$s", c.get() == 1 ? "" : "s"))));
    }
}
