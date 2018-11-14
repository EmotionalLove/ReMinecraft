package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.ingame.client.world.ClientTeleportConfirmPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.event.ServerResetPlayerPositionEvent;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.reaction.IPacketReactor;

public class ServerPlayerPositionRotationReaction implements IPacketReactor<ServerPlayerPositionRotationPacket> {
    @Override
    public boolean takeAction(ServerPlayerPositionRotationPacket packet) {
        ServerResetPlayerPositionEvent resetEvent = new ServerResetPlayerPositionEvent(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch());
        ReMinecraft.INSTANCE.EVENT_BUS.invokeEvent(resetEvent);
        ReClient.ReClientCache.INSTANCE.posX = packet.getX();
        ReClient.ReClientCache.INSTANCE.posY = packet.getY();
        ReClient.ReClientCache.INSTANCE.posZ = packet.getZ();
        ReClient.ReClientCache.INSTANCE.pitch = packet.getPitch();
        ReClient.ReClientCache.INSTANCE.yaw = packet.getYaw();
        if (!ReMinecraft.INSTANCE.areChildrenConnected()) {
            // the notchian client will do this for us, if one is connected
            ReMinecraft.INSTANCE.minecraftClient.getSession().send(new ClientTeleportConfirmPacket(packet.getTeleportId()));
        }
        return true;
    }
}
