package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.event.ChatReceivedEvent;
import com.sasha.reminecraft.reaction.IPacketReactor;

public class ServerChatReaction implements IPacketReactor<ServerChatPacket> {
    @Override
    public boolean takeAction(ServerChatPacket packet) {
        Message pckMsg = Message.fromJson(removeEvents(packet.getMessage().toJson().getAsJsonObject()));
        ChatReceivedEvent chatEvent = new ChatReceivedEvent(pckMsg.getFullText(), System.currentTimeMillis());
        ReMinecraft.INSTANCE.EVENT_BUS.invokeEvent(chatEvent);
        ReMinecraft.INSTANCE.logger.log("(CHAT) " + pckMsg.getFullText());
        ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(Message.fromJson(pckMsg.toJson()), packet.getType()));
        return false;
    }

    /**
     * This is supposed to fix the issue where death messages on 2b2t would show up
     * as raw JSON, but it doesn't work
     *
     * @086 help ;-;
     *
     * @param object a json boi
     * @return the fixed json boi
     */
    private static JsonObject removeEvents(JsonObject object) {
        if (object.has("extra")) {
            JsonArray extra = object.getAsJsonArray("extra");
            object.remove("extra");
            for (int i = 0; i < extra.size(); i++) {
                JsonObject extraObject = extra.get(i).getAsJsonObject();
                extra.remove(extraObject);
                extra.add(removeEvents(extraObject));
            }
            object.add("extra", extra);
        }
        object.remove("clickEvent");
        object.remove("hoverEvent");
        object.remove("insertion");
        return object;
    }

}
