package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityMovementPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.util.entity.Entity;
import com.sasha.reminecraft.util.entity.EntityRotation;

import java.lang.reflect.Field;

public class ServerEntityMovementReaction implements IPacketReactor<ServerEntityMovementPacket> {
    @Override
    public boolean takeAction(ServerEntityMovementPacket packet) {
        try {
            Entity e = ReClient.ReClientCache.INSTANCE.entityCache.get(packet.getEntityId());
            if (e == null) {
                ReMinecraft.INSTANCE.terminalLogger.logDebug
                        ("Null entity with entity id " + packet.getEntityId());
                ReMinecraft.INSTANCE.sendToChildren(packet);
                return false;
            }
            e.posX += packet.getMovementX() / 4096d;
            e.posY += packet.getMovementY() / 4096d;
            e.posZ += packet.getMovementZ() / 4096d;
            boolean flag;
            Field field = ServerEntityMovementPacket.class.getDeclaredField("rot");
            field.setAccessible(true); // leet hax
            flag = (boolean) field.get(packet);
            if (flag && e instanceof EntityRotation) {
                ((EntityRotation) e).yaw = packet.getYaw();
                ((EntityRotation) e).pitch = packet.getPitch();
            }
        }catch (NoSuchFieldException | IllegalAccessException ignored) {
            //
        }
        return true;
    }
}
