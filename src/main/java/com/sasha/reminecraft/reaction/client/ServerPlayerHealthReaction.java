package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.data.game.ClientRequest;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientRequestPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerHealthPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.event.PlayerDamagedEvent;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.reaction.IPacketReactor;

public class ServerPlayerHealthReaction implements IPacketReactor<ServerPlayerHealthPacket> {
    @Override
    public boolean takeAction(ServerPlayerHealthPacket packet) {
        PlayerDamagedEvent damagedEvent = new PlayerDamagedEvent(ReClient.ReClientCache.INSTANCE.health, packet.getHealth());
        ReMinecraft.INSTANCE.EVENT_BUS.invokeEvent(damagedEvent);
        ReClient.ReClientCache.INSTANCE.health = packet.getHealth();
        ReClient.ReClientCache.INSTANCE.food = packet.getFood();
        ReClient.ReClientCache.INSTANCE.saturation = packet.getSaturation();
        if (ReClient.ReClientCache.INSTANCE.health <= 0.0f) {
            ReMinecraft.INSTANCE.minecraftClient.getSession().send(new ClientRequestPacket(ClientRequest.RESPAWN));
        }
        return true;
    }
}
