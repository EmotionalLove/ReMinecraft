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
        Message pckMsg = packet.getMessage();
        ChatRecievedEvent chatEvent = new ChatRecievedEvent(packet.getMessage().getFullText(), System.currentTimeMillis());
        ReMinecraft.INSTANCE.EVENT_BUS.invokeEvent(chatEvent);
        ReMinecraft.INSTANCE.logger.log("(CHAT) " + packet.getMessage().getFullText());
        for (Message message : pckMsg.getExtra()) {
            if (message.getStyle().getHoverEvent() != null) {
                message.getStyle().setHoverEvent(null);
            }
        }
        JsonElement elem = pckMsg.toJson();
        ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(Message.fromJson(elem), packet.getType()));
        return false;
    }
}
