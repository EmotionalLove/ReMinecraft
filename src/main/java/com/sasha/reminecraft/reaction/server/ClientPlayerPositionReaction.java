package com.sasha.reminecraft.reaction.server;

import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionPacket;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.reaction.IPacketReactor;

public class ClientPlayerPositionReaction implements IPacketReactor<ClientPlayerPositionPacket> {
    @Override
    public boolean takeAction(ClientPlayerPositionPacket packet) {
        ReClient.ReClientCache.INSTANCE.posX = packet.getX();
        ReClient.ReClientCache.INSTANCE.player.posX = packet.getX();
        ReClient.ReClientCache.INSTANCE.posY = packet.getY();
        ReClient.ReClientCache.INSTANCE.player.posY = packet.getY();
        ReClient.ReClientCache.INSTANCE.posZ = packet.getZ();
        ReClient.ReClientCache.INSTANCE.player.posZ = packet.getZ();
        ReClient.ReClientCache.INSTANCE.onGround = packet.isOnGround();
        return true;
    }

}
