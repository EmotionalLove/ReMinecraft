package com.sasha.reminecraft.util.entity;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.EntityMetadata;
import com.sasha.reminecraft.client.ReListener;

import java.util.UUID;

public abstract class Entity {
    public EntityType type;
    public double x, y, z;
    public int entityId;
    public UUID uuid;
    public EntityMetadata metadata[] = new EntityMetadata[0];

    public static Entity getEntityByID(int id) {
        for (Entity entity : ReListener.ReListenerCache.entityCache.values()) {
            if (entity.entityId == id) {
                return entity;
            }
        }

        return null;
    }

    public static Entity getEntityBeingRiddenBy(int entityId) {
        for (Entity entity1 : ReListener.ReListenerCache.entityCache.values()) {
            for (int pID : ((EntityEquipment) entity1).passengerIds) {
                if (pID == entityId) {
                    return entity1;
                }
            }
        }
        return null;
    }
}