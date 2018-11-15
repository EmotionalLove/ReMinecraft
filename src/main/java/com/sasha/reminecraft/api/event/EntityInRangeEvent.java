package com.sasha.reminecraft.api.event;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.sasha.eventsys.SimpleEvent;
import com.sasha.reminecraft.client.ReClient;

import java.util.UUID;

public class EntityInRangeEvent {

    /**
     * Describes a Player that comes into range
     */
    public static class Player extends SimpleEvent {

        private String name;
        private UUID id;
        private int entityId;

        public Player(UUID id, int entityId) {
            this.id = id;
            this.entityId = entityId;
            GameProfile profile = ReClient.ReClientCache.INSTANCE.getGameProfileByUuid(id);
            if (profile != null && id != null) {
                this.name = profile.getName();
            }
        }

        public String getName() {
            return name;
        }

        public UUID getId() {
            return id;
        }

        public int getEntityId() {
            return entityId;
        }
    }

    /**
     * Describes a generic entity that comes into range
     */
    public static class Entity extends SimpleEvent {

        private UUID id;
        private int entityId;

        public Entity(UUID id, int entityId) {
            this.id = id;
            this.entityId = entityId;
        }


        public UUID getId() {
            return id;
        }

        public int getEntityId() {
            return entityId;
        }
    }

}
