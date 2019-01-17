package com.sasha.reminecraft.util.entity;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.EntityMetadata;
import com.sasha.reminecraft.client.ReClient;

import java.util.ArrayList;
import java.util.UUID;

public abstract class Entity {
    public EntityType type;
    public double posX, posY, posZ;
    public int entityId;
    public UUID uuid;
    public EntityMetadata[] metadata = new EntityMetadata[0];
    public ArrayList<Integer> passengerIds = new ArrayList<>();

    public int[] passengersAsArray() {
        int[] arr = new int[passengerIds.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = passengerIds.get(i);
        }
        return arr;
    }


    public void addEntity(int eid) {
        passengerIds.add(eid);
    }
    public void addEntity(int[] eid) {
        for (int i : eid) {
            addEntity(i);
        }
    }
    public void setEntity(int[] eid) {
        passengerIds.clear();
        for (int i : eid) {
            addEntity(i);
        }
    }
    public void clearEntity() {
        passengerIds.clear();
    }

    public static Entity getEntityByID(int id) {
        for (Entity entity : ReClient.ReClientCache.INSTANCE.entityCache.values()) {
            if (entity.entityId == id) {
                return entity;
            }
        }

        return null;
    }

    public static Entity getEntityBeingRiddenBy(int entityId) {
        for (Entity entity1 : ReClient.ReClientCache.INSTANCE.entityCache.values()) {
            for (int pID : ((EntityEquipment) entity1).passengerIds) {
                if (pID == entityId) {
                    return entity1;
                }
            }
        }
        return null;
    }
}