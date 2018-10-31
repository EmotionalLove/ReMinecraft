package com.sasha.reminecraft.reaction.server;

import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.reaction.IPacketReactor;

public class ClientPlayerPositionRotationReaction implements IPacketReactor<ClientPlayerPositionRotationPacket> {

    @Override
    public boolean takeAction(ClientPlayerPositionRotationPacket packet) {
        ReClient.ReClientCache.INSTANCE.posX = packet.getX();
        ReClient.ReClientCache.INSTANCE.player.posX = packet.getX();
        ReClient.ReClientCache.INSTANCE.posY = packet.getY();
        ReClient.ReClientCache.INSTANCE.player.posY = packet.getY();
        ReClient.ReClientCache.INSTANCE.posZ = packet.getZ();
        ReClient.ReClientCache.INSTANCE.player.posZ = packet.getZ();
        ReClient.ReClientCache.INSTANCE.yaw = (float) packet.getYaw();
        ReClient.ReClientCache.INSTANCE.player.yaw = (float) packet.getYaw();
        ReClient.ReClientCache.INSTANCE.pitch = (float) packet.getPitch();
        ReClient.ReClientCache.INSTANCE.player.pitch = (float) packet.getPitch();
        ReClient.ReClientCache.INSTANCE.onGround = packet.isOnGround();
        return true;
    }

}
