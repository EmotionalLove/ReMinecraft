package com.sasha.reminecraft.util;

import com.github.steveice10.mc.protocol.data.message.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * from https://github.com/Steveice10/MCProtocolLib/commit/050ddd3911ed989406b62ee5fa23d8a147e615a7
 */
public class MessageUtil {

    public static Message fromString(String str) {
        try {
            return fromJson(new JsonParser().parse(str));
        } catch(Exception e) {
            return new TextMessage(str);
        }
    }

    public static Message fromJson(JsonElement e) {
        if(e.isJsonPrimitive()) {
            return new TextMessage(e.getAsString());
        } else if(e.isJsonArray()) {
            JsonArray array = e.getAsJsonArray();
            if(array.size() == 0) {
                return new TextMessage("");
            }

            Message msg = Message.fromJson(array.get(0));
            for(int index = 1; index < array.size(); index++) {
                msg.addExtra(Message.fromJson(array.get(index)));
            }

            return msg;
        } else if(e.isJsonObject()) {
            JsonObject json = e.getAsJsonObject();
            Message msg;
            if(json.has("text")) {
                msg = new TextMessage(json.get("text").getAsString());
            } else if(json.has("translate")) {
                Message[] with = new Message[0];
                if(json.has("with")) {
                    JsonArray withJson = json.get("with").getAsJsonArray();
                    with = new Message[withJson.size()];
                    for(int index = 0; index < withJson.size(); index++) {
                        JsonElement el = withJson.get(index);
                        if(el.isJsonPrimitive()) {
                            with[index] = new TextMessage(el.getAsString());
                        } else {
                            with[index] = Message.fromJson(el.getAsJsonObject());
                        }
                    }
                }

                msg = new TranslationMessage(json.get("translate").getAsString(), with);
            } else if(json.has("keybind")) {
                msg = new KeybindMessage(json.get("keybind").getAsString());
            } else {
                throw new IllegalArgumentException("Unknown message type in json: " + json.toString());
            }

            MessageStyle style = new MessageStyle();
            if(json.has("color")) {
                style.setColor(ChatColor.byName(json.get("color").getAsString()));
            }

            for(ChatFormat format : ChatFormat.values()) {
                if(json.has(format.toString()) && json.get(format.toString()).getAsBoolean()) {
                    style.addFormat(format);
                }
            }

            if(json.has("clickEvent")) {
                JsonObject click = json.get("clickEvent").getAsJsonObject();
                style.setClickEvent(new ClickEvent(ClickAction.byName(click.get("action").getAsString()), click.get("value").getAsString()));
            }

            if(json.has("hoverEvent")) {
                JsonObject hover = json.get("hoverEvent").getAsJsonObject();
                style.setHoverEvent(new HoverEvent(HoverAction.byName(hover.get("action").getAsString()), Message.fromJson(hover.get("value"))));
            }

            if(json.has("insertion")) {
                style.setInsertion(json.get("insertion").getAsString());
            }

            msg.setStyle(style);

            if(json.has("extra")) {
                JsonArray extraJson = json.get("extra").getAsJsonArray();
                for(int index = 0; index < extraJson.size(); index++) {
                    msg.addExtra(Message.fromJson(extraJson.get(index)));
                }
            }

            return msg;
        } else {
            throw new IllegalArgumentException("Cannot convert " + e.getClass().getSimpleName() + " to a message.");
        }
    }
}
