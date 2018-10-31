package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.google.gson.JsonElement;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.event.ChatRecievedEvent;
import com.sasha.reminecraft.reaction.IPacketReactor;

public class ServerChatReaction implements IPacketReactor<ServerChatPacket> {
    @Override
    public boolean takeAction(ServerChatPacket packet) {
        ChatRecievedEvent chatEvent = new ChatRecievedEvent(packet.getMessage().getFullText(), System.currentTimeMillis());
        ReMinecraft.INSTANCE.EVENT_BUS.invokeEvent(chatEvent);
        ReMinecraft.INSTANCE.logger.log("(CHAT) " + packet.getMessage().getFullText());
        JsonElement msg = packet.getMessage().toJson();
        ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(Message.fromJson(msg), packet.getType()));
        return false;
    }
}
