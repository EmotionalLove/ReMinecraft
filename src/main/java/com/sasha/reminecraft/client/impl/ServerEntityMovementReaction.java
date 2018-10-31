package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityMovementPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.entity.Entity;
import com.sasha.reminecraft.util.entity.EntityRotation;

import java.lang.reflect.Field;

public class ServerEntityMovementReaction implements IPacketReactor<ServerEntityMovementPacket> {
    @Override
    public boolean takeAction(ServerEntityMovementPacket pck) {
        try {
            Entity e = ReClient.ReClientCache.INSTANCE.entityCache.get(pck.getEntityId());
            if (e == null) {
                ReMinecraft.INSTANCE.logger.logDebug
                        ("Null entity with entity id " + pck.getEntityId());
                ReMinecraft.INSTANCE.sendToChildren(pck);
                return false;
            }
            e.posX += pck.getMovementX() / 4096d;
            e.posY += pck.getMovementY() / 4096d;
            e.posZ += pck.getMovementZ() / 4096d;
            boolean flag;
            Field field = ServerEntityMovementPacket.class.getDeclaredField("rot");
            field.setAccessible(true); // leet hax
            flag = (boolean) field.get(pck);
            if (flag && e instanceof EntityRotation) {
                ((EntityRotation) e).yaw = pck.getYaw();
                ((EntityRotation) e).pitch = pck.getPitch();
            }
        }catch (NoSuchFieldException | IllegalAccessException ignored) {
            //
        }
        return true;
    }
}
