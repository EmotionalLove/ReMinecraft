package com.sasha.reminecraft.util.entity;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.EntityMetadata;
import com.sasha.reminecraft.client.ReClient;

import java.util.UUID;

public abstract class Entity {
    public EntityType type;
    public double posX, posY, posZ;
    public int entityId;
    public UUID uuid;
    public EntityMetadata metadata[] = new EntityMetadata[0];

    public static Entity getEntityByID(int id) {
        for (Entity entity : ReClient.ReClientCache.entityCache.values()) {
            if (entity.entityId == id) {
                return entity;
            }
        }

        return null;
    }

    public static Entity getEntityBeingRiddenBy(int entityId) {
        for (Entity entity1 : ReClient.ReClientCache.entityCache.values()) {
            for (int pID : ((EntityEquipment) entity1).passengerIds) {
                if (pID == entityId) {
                    return entity1;
                }
            }
        }
        return null;
    }
}