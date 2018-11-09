package com.sasha.reminecraft.api.event;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.sasha.eventsys.SimpleEvent;

import java.util.UUID;

/**
 * Invoked when another player quits the server that RE:Minecraft is connected to (may not work on servers with modified tablists)
 */
public class ServerOtherPlayerQuitEvent extends SimpleEvent {

    public String name;
    public UUID uuid;

    public ServerOtherPlayerQuitEvent(GameProfile profile) {
        this.name = profile.getName();
        this.uuid = profile.getId();
    }

    public ServerOtherPlayerQuitEvent(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }
    
    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }
}
