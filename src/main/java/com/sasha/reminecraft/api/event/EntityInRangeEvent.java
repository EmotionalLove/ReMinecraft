package com.sasha.reminecraft.api.event;

import java.util.UUID;

public class EntityInRangeEvent {

    /**
     * Describes a Player that comes into range
     */
    public static class Player {

        private String name;
        private UUID id;
        private int entityId;

        public Player(UUID id, int entityId) {
            this.id = id;
            this.entityId = entityId;
            if (id != null) {

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
    public static class Entity {

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
