package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.client.world.ClientTeleportConfirmPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.event.ServerResetPlayerPositionEvent;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;

public class ServerPlayerPositionRotationReaction implements IPacketReactor<ServerPlayerPositionRotationPacket> {
    @Override
    public boolean takeAction(ServerPlayerPositionRotationPacket pck) {
        ServerResetPlayerPositionEvent resetEvent = new ServerResetPlayerPositionEvent(pck.getX(), pck.getY(), pck.getZ(), pck.getYaw(), pck.getPitch());
        ReMinecraft.INSTANCE.EVENT_BUS.invokeEvent(resetEvent);
        ReClient.ReClientCache.INSTANCE.posX = pck.getX();
        ReClient.ReClientCache.INSTANCE.posY = pck.getY();
        ReClient.ReClientCache.INSTANCE.posZ = pck.getZ();
        ReClient.ReClientCache.INSTANCE.pitch = pck.getPitch();
        ReClient.ReClientCache.INSTANCE.yaw = pck.getYaw();
        if (!ReMinecraft.INSTANCE.areChildrenConnected()) {
            // the notchian client will do this for us, if one is connected
            ReMinecraft.INSTANCE.minecraftClient.getSession().send(new ClientTeleportConfirmPacket(pck.getTeleportId()));
        }
        return true;
    }
}
