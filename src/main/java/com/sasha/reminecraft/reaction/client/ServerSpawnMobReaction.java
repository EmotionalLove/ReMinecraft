package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnMobPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.event.EntityInRangeEvent;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.util.entity.EntityMob;
import com.sasha.reminecraft.util.entity.EntityType;

public class ServerSpawnMobReaction implements IPacketReactor<ServerSpawnMobPacket> {
    @Override
    public boolean takeAction(ServerSpawnMobPacket packet) {
        EntityMob e = new EntityMob();
        e.type = EntityType.MOB;
        e.entityId = packet.getEntityId();
        e.uuid = packet.getUUID();
        e.mobType = packet.getType();
        e.posX = packet.getX();
        e.posY = packet.getY();
        e.posZ = packet.getZ();
        e.pitch = packet.getPitch();
        e.yaw = packet.getYaw();
        e.headYaw = packet.getHeadYaw();
        e.motionX = packet.getMotionX();
        e.motionY = packet.getMotionY();
        e.motionZ = packet.getMotionZ();
        e.metadata = packet.getMetadata();
        ReClient.ReClientCache.INSTANCE.entityCache.put(e.entityId, e);
        EntityInRangeEvent.Entity event = new EntityInRangeEvent.Entity(e.uuid, e.entityId);
        ReMinecraft.INSTANCE.EVENT_BUS.invokeEvent(event);
        return true;
    }
}
