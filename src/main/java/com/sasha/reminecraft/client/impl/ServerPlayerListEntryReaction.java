package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPlayerListEntryPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;

import java.lang.reflect.Field;
import java.util.*;

public class ServerPlayerListEntryReaction implements IPacketReactor<ServerPlayerListEntryPacket> {
    @Override
    public boolean takeAction(ServerPlayerListEntryPacket pck) {
        switch (pck.getAction()) {
            case ADD_PLAYER:
                Arrays.stream(pck.getEntries())
                        .filter(e -> !ReClient.ReClientCache.INSTANCE.playerListEntries.contains(e))
                        .forEach(entry -> ReClient.ReClientCache.INSTANCE.playerListEntries.add(entry));
                break;
            case REMOVE_PLAYER:
                List<String> toRemove = new ArrayList<>();
                List<Integer> removalIndexes = new ArrayList<>();
                Arrays.stream(pck.getEntries()).forEach(entry -> toRemove.add(entry.getProfile().getId().toString()));
                ReClient.ReClientCache.INSTANCE.playerListEntries.forEach(entry -> {
                    if (toRemove.contains(entry.getProfile().getId().toString())) {
                        removalIndexes.add(ReClient.ReClientCache.INSTANCE.playerListEntries.indexOf(entry));
                    }
                });
                removalIndexes.forEach(index -> {
                    ReClient.ReClientCache.INSTANCE.playerListEntries.remove((int)index);
                });
                break;
            case UPDATE_DISPLAY_NAME:
                LinkedHashMap<UUID, Message> changeMap = new LinkedHashMap<>();
                for (PlayerListEntry entry : pck.getEntries()) {
                    changeMap.put(entry.getProfile().getId(), entry.getDisplayName());
                }
                changeMap.forEach((id, msg) -> {
                    for (PlayerListEntry playerListEntry : ReClient.ReClientCache.INSTANCE.playerListEntries) {
                        if (playerListEntry.getProfile().getId().equals(id)) {
                            try {
                                Field field = playerListEntry.getClass().getDeclaredField("displayName");
                                field.setAccessible(true);
                                field.set(playerListEntry, msg);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                break;
            case UPDATE_LATENCY:
                LinkedHashMap<UUID, Integer> pingMap = new LinkedHashMap<>();
                for (PlayerListEntry entry : pck.getEntries()) {
                    pingMap.put(entry.getProfile().getId(), entry.getPing());
                }
                pingMap.forEach((id, ping) -> {
                    for (PlayerListEntry playerListEntry : ReClient.ReClientCache.INSTANCE.playerListEntries) {
                        if (playerListEntry.getProfile().getId().equals(id)) {
                            try {
                                Field field = playerListEntry.getClass().getDeclaredField("ping");
                                field.setAccessible(true);
                                field.set(playerListEntry, ping);
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                break;
            case UPDATE_GAMEMODE:
                LinkedHashMap<UUID, GameMode> gamemodeMap = new LinkedHashMap<>();
                for (PlayerListEntry entry : pck.getEntries()) {
                    gamemodeMap.put(entry.getProfile().getId(), entry.getGameMode());
                }
                gamemodeMap.forEach((id, gm) -> {
                    for (PlayerListEntry playerListEntry : ReClient.ReClientCache.INSTANCE.playerListEntries) {
                        if (playerListEntry.getProfile().getId().equals(id)) {
                            try {
                                Field field = playerListEntry.getClass().getDeclaredField("gameMode");
                                field.setAccessible(true);
                                field.set(playerListEntry, gm);
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                break;
            default:
                ReMinecraft.INSTANCE.logger.logError("Unsupported tablist action!");

        }
        return true;
    }
}
