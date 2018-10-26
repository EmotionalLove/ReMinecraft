package com.sasha.reminecraft.command.game;

import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.simplecmdsys.SimpleCommand;

public class TestCommand extends SimpleCommand {

    public TestCommand() {
        super("test");
    }

    @Override
    public void onCommand() {
        ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(Message.fromString("\247bTest complete!")));
    }
}
