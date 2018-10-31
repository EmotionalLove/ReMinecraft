package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.google.gson.JsonElement;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.event.ChatRecievedEvent;
import com.sasha.reminecraft.client.IPacketReactor;

public class ServerChatReaction implements IPacketReactor<ServerChatPacket> {
    @Override
    public boolean takeAction(ServerChatPacket pck) {
        ChatRecievedEvent chatEvent = new ChatRecievedEvent(pck.getMessage().getFullText(), System.currentTimeMillis());
        ReMinecraft.INSTANCE.EVENT_BUS.invokeEvent(chatEvent);
        ReMinecraft.INSTANCE.logger.log("(CHAT) " + pck.getMessage().getFullText());
        JsonElement msg = pck.getMessage().toJson();
        ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(Message.fromJson(msg), pck.getType()));
        return false;
    }
}
