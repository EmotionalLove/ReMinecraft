package com.sasha.reminecraft.client;

import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.github.steveice10.mc.protocol.data.game.world.block.BlockChangeRecord;
import com.github.steveice10.mc.protocol.data.game.world.notify.ClientNotification;
import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.packet.ingame.client.world.ClientTeleportConfirmPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerHealthPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnMobPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnObjectPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerWindowItemsPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.*;
import com.github.steveice10.mc.protocol.packet.login.server.LoginDisconnectPacket;
import com.github.steveice10.mc.protocol.packet.login.server.LoginSuccessPacket;
import com.github.steveice10.packetlib.event.session.*;
import com.github.steveice10.packetlib.packet.Packet;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.client.children.ChildReClient;
import com.sasha.reminecraft.util.ChunkUtil;
import com.sasha.reminecraft.util.entity.*;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens and processes packets being Tx'd and Rx'd from the remote server.
 */
public class ReListener implements SessionListener {

    /**
     * Invoked when a packet is recieved
     */
    @Override
    public void packetReceived(PacketReceivedEvent event) {
        try {
            if (event.getPacket() instanceof ServerChatPacket) {
                ReMinecraft.INSTANCE.logger.log("(CHAT) " + ((ServerChatPacket) event.getPacket()).getMessage().getFullText());
            }
            if (event.getPacket() instanceof ServerPlayerHealthPacket) {
                //update player health
                var pck = (ServerPlayerHealthPacket) event.getPacket();
                ReListenerCache.health = pck.getHealth();
                ReListenerCache.food = pck.getFood();
                ReListenerCache.saturation = pck.getSaturation();
                if (ReListenerCache.health <= 0f) {
                    // todo autorespawn
                }
            }
            if (event.getPacket() instanceof ServerPlayerListEntryPacket) {
                var pck = (ServerPlayerListEntryPacket) event.getPacket();
                switch (pck.getAction()) {
                    case ADD_PLAYER:
                        Arrays.stream(pck.getEntries())
                                .filter(e -> !ReListenerCache.playerListEntries.contains(e))
                                .forEach(entry -> ReListenerCache.playerListEntries.add(entry));
                        break;
                    case REMOVE_PLAYER:
                        Arrays.stream(pck.getEntries())
                                .filter(e -> ReListenerCache.playerListEntries.contains(e))
                                .forEach(entry -> ReListenerCache.playerListEntries.remove(entry));
                        break;
                    case UPDATE_DISPLAY_NAME:
                        LinkedHashMap<UUID, Message> changeMap = new LinkedHashMap<>();
                        for (PlayerListEntry entry : pck.getEntries()) {
                            changeMap.put(entry.getProfile().getId(), entry.getDisplayName());
                        }
                        changeMap.forEach((id, msg) -> {
                            for (PlayerListEntry playerListEntry : ReListenerCache.playerListEntries) {
                                if (playerListEntry.getProfile().getId().equals(id)) {
                                    try {
                                        var field = playerListEntry.getClass().getDeclaredField("displayName");
                                        field.setAccessible(true);
                                        field.set(playerListEntry, msg);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                        break;
                    case UPDATE_LATENCY:
                        LinkedHashMap<UUID, Integer> pingMap = new LinkedHashMap<>();
                        for (PlayerListEntry entry : pck.getEntries()) {
                            pingMap.put(entry.getProfile().getId(), entry.getPing());
                        }
                        pingMap.forEach((id, ping) -> {
                            for (PlayerListEntry playerListEntry : ReListenerCache.playerListEntries) {
                                if (playerListEntry.getProfile().getId().equals(id)) {
                                    try {
                                        var field = playerListEntry.getClass().getDeclaredField("ping");
                                        field.setAccessible(true);
                                        field.set(playerListEntry, ping);
                                    } catch (NoSuchFieldException | IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                        break;
                    case UPDATE_GAMEMODE:
                        LinkedHashMap<UUID, GameMode> gamemodeMap = new LinkedHashMap<>();
                        for (PlayerListEntry entry : pck.getEntries()) {
                            gamemodeMap.put(entry.getProfile().getId(), entry.getGameMode());
                        }
                        gamemodeMap.forEach((id, ping) -> {
                            for (PlayerListEntry playerListEntry : ReListenerCache.playerListEntries) {
                                if (playerListEntry.getProfile().getId().equals(id)) {
                                    ReListenerCache.playerListEntries.remove(playerListEntry);
                                    break;
                                }
                            }
                        });
                        ReListenerCache.playerListEntries.addAll(Arrays.asList(pck.getEntries()));
                        break;
                    default:
                        ReMinecraft.INSTANCE.logger.logError("Unsupported tablist action!");

                }
            }
            if (event.getPacket() instanceof ServerPlayerListDataPacket) {
                ReListenerCache.tabHeader = ((ServerPlayerListDataPacket) event.getPacket()).getHeader();
                ReListenerCache.tabFooter = ((ServerPlayerListDataPacket) event.getPacket()).getFooter();
            }
            if (event.getPacket() instanceof ServerPlayerPositionRotationPacket) {
                var pck = (ServerPlayerPositionRotationPacket) event.getPacket();
                ReListenerCache.posX = pck.getX();
                ReListenerCache.posY = pck.getY();
                ReListenerCache.posZ = pck.getZ();
                ReListenerCache.pitch = pck.getPitch();
                ReListenerCache.yaw = pck.getYaw();
                if (!ReMinecraft.INSTANCE.areChildrenConnected()) {
                    // the notchian client will do this for us, if one is connected
                    ReMinecraft.INSTANCE.minecraftClient.getSession().send(new ClientTeleportConfirmPacket(pck.getTeleportId()));
                }
            }
            if (event.getPacket() instanceof ServerChunkDataPacket) {
                // VERY IMPORTANT: Chunks will NOT RENDER correctly and be invisible on notchian clients if we
                // do not actually push them correctly. This is apparent with big chunks and newly generated ones
                // that need to be dispersed over multiple packets. Trust me, it's really gay.
                // btw i love phi <33333333333333333 hes like super nice
                var pck = (ServerChunkDataPacket) event.getPacket();
                Column column = pck.getColumn();
                long hash = ChunkUtil.getChunkHashFromXZ(column.getX(), column.getZ());
                if (!column.hasBiomeData()) {
                    // if the chunk is thicc or newly generated
                    if (ReListenerCache.chunkCache.containsKey(hash)) {
                        Column chunkToAddTo = ReListenerCache.chunkCache.get(hash);
                        this.sendToChildren(new ServerUnloadChunkPacket(ChunkUtil.getXFromHash(hash),
                                ChunkUtil.getZFromHash(hash)));
                        for (int i = 0; i <= 15; i++) {
                            if (column.getChunks()[i] != null) {
                                chunkToAddTo.getChunks()[i] = column.getChunks()[i];
                            }
                        }
                        ReListenerCache.chunkCache.put(hash, chunkToAddTo);
                        this.sendToChildren(new ServerChunkDataPacket(chunkToAddTo));
                    }
                } else {
                    ReListenerCache.chunkCache.put(hash, pck.getColumn());
                }
            }
            if (event.getPacket() instanceof ServerUnloadChunkPacket) {
                var pck = (ServerUnloadChunkPacket) event.getPacket();
                long hash = ChunkUtil.getChunkHashFromXZ(pck.getX(), pck.getZ());
                ReListenerCache.chunkCache.remove(hash);
            }
            if (event.getPacket() instanceof ServerUpdateTimePacket) {
                // todo
            }
            if (event.getPacket() instanceof ServerBlockChangePacket) {
                // update the cached chunks
                // sometimes inaccurate (i think?)
                var pck = (ServerBlockChangePacket) event.getPacket();
                int chunkX = pck.getRecord().getPosition().getX() >> 4;
                int chunkZ = pck.getRecord().getPosition().getZ() >> 4;
                int cubeY = ChunkUtil.clamp(pck.getRecord().getPosition().getY() >> 4, 0, 15);
                Column column = ReListenerCache.chunkCache
                        .getOrDefault(ChunkUtil.getChunkHashFromXZ(chunkX, chunkZ), null);
                if (column == null) {
                    // not ignoring this can leak memory in the notchian client
                    ReMinecraft.INSTANCE.logger.logWarning("Ignoring server request to change blocks in an unloaded chunk, is the remote server running a modified Minecraft server jar? This could cause issues.");
                    return;
                }
                Chunk subChunk = column.getChunks()[cubeY];
                int cubeRelY = Math.abs(pck.getRecord().getPosition().getY() - 16 * cubeY);
                try {
                    subChunk.getBlocks().set(Math.abs(Math.abs(pck.getRecord().getPosition().getX()) - (Math.abs(Math.abs(pck.getRecord().getPosition().getX() >> 4)) * 16)), ChunkUtil.clamp(cubeRelY, 0, 15), Math.abs(Math.abs(pck.getRecord().getPosition().getZ()) - (Math.abs(Math.abs(pck.getRecord().getPosition().getZ() >> 4)) * 16)), pck.getRecord().getBlock());
                    column.getChunks()[cubeY] = subChunk;
                    ReListenerCache.chunkCache.put(ChunkUtil.getChunkHashFromXZ(chunkX, chunkZ), column);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ReListenerCache.chunkCache.put(ChunkUtil.getChunkHashFromXZ(chunkX, chunkZ), column);
            }
            if (event.getPacket() instanceof ServerMultiBlockChangePacket) {
                // this is more complicated
                var pck = (ServerMultiBlockChangePacket) event.getPacket();
                int chunkX = pck.getRecords()[0].getPosition().getX() >> 4;
                int chunkZ = pck.getRecords()[0].getPosition().getZ() >> 4;
                Column column = ReListenerCache.chunkCache.getOrDefault(ChunkUtil.getChunkHashFromXZ(chunkX, chunkZ), null);
                if (column == null) {
                    // not ignoring this can leak memory in the notchian client
                    ReMinecraft.INSTANCE.logger.logWarning("Ignoring server request to change blocks in an unloaded chunk, is the remote server running a modified Minecraft server jar? This could cause issues.");
                    return;
                }
                for (BlockChangeRecord record : pck.getRecords()) {
                    int relativeChunkX = Math.abs(Math.abs(record.getPosition().getX()) - (Math.abs(Math.abs(record.getPosition().getX() >> 4)) * 16));
                    int relativeChunkZ = Math.abs(Math.abs(record.getPosition().getZ()) - (Math.abs(Math.abs(record.getPosition().getZ() >> 4)) * 16));
                    int cubeY = ChunkUtil.clamp(record.getPosition().getY() >> 4, 0, 15);
                    Chunk cube = column.getChunks()[cubeY];
                    int cubeRelativeY = Math.abs(record.getPosition().getY() - 16 * cubeY);
                    try {
                        cube.getBlocks().set(relativeChunkX, ChunkUtil.clamp(cubeRelativeY, 0, 15), relativeChunkZ, record.getBlock());
                        column.getChunks()[cubeY] = cube;
                    } catch (Exception e) {
                        System.out.println(relativeChunkX + " " + cubeRelativeY + " " + relativeChunkZ + " " + (cubeRelativeY << 8 | relativeChunkZ << 4 | relativeChunkX));
                    }
                }
                ReListenerCache.chunkCache.put(ChunkUtil.getChunkHashFromXZ(chunkX, chunkZ), column);
            }
            if (event.getPacket() instanceof ServerJoinGamePacket) {
                // this is when YOU join the server, not another player
                var pck = (ServerJoinGamePacket) event.getPacket();
                ReListenerCache.dimension = pck.getDimension();
                ReListenerCache.entityId = pck.getEntityId();
                ReListenerCache.gameMode = pck.getGameMode();
                EntityPlayer player = new EntityPlayer();
                player.type = EntityType.REAL_PLAYER;
                player.entityId = ReListenerCache.entityId;
                player.uuid = ReListenerCache.uuid;
                ReListenerCache.player = player;
                ReListenerCache.entityCache.put(player.entityId, player);
            }
            if (event.getPacket() instanceof LoginSuccessPacket) {
                ReListenerCache.uuid = ((LoginSuccessPacket) event.getPacket()).getProfile().getId();
            }
            if (event.getPacket() instanceof ServerNotifyClientPacket) {
                var pck = (ServerNotifyClientPacket) event.getPacket();
                if (pck.getNotification() == ClientNotification.CHANGE_GAMEMODE) {
                    ReListenerCache.gameMode = (GameMode) pck.getValue();
                }
            }
            if (event.getPacket() instanceof ServerRespawnPacket) {
                // clear everything because none of it matters now :smiling_imp:
                var pck = (ServerRespawnPacket) event.getPacket();
                ReListenerCache.dimension = pck.getDimension();
                ReListenerCache.gameMode = pck.getGameMode();
                ReListenerCache.chunkCache.clear();
                ReListenerCache.entityCache.entrySet().removeIf(integerEntityEntry -> integerEntityEntry.getKey() != ReListenerCache.entityId);
                ReListenerCache.cachedBossBars.clear();
                ReListenerCache.player.potionEffects.clear();
            }
            if (event.getPacket() instanceof LoginDisconnectPacket) {
                var pck = (LoginDisconnectPacket) event.getPacket();
                ReMinecraft.INSTANCE.logger.logError("Kicked whilst logging in: " + pck.getReason().getFullText());
                ReMinecraft.INSTANCE.minecraftClient.getSession().disconnect(pck.getReason().getFullText(), true);
            }
            if (event.getPacket() instanceof ServerSpawnMobPacket) {
                var pck = (ServerSpawnMobPacket) event.getPacket();
                EntityMob e = new EntityMob();
                e.type = EntityType.MOB;
                e.entityId = pck.getEntityId();
                e.uuid = pck.getUUID();
                e.mobType = pck.getType();
                e.posX = pck.getX();
                e.posY = pck.getY();
                e.posZ = pck.getZ();
                e.pitch = pck.getPitch();
                e.yaw = pck.getYaw();
                e.headYaw = pck.getHeadYaw();
                e.motionX = pck.getMotionX();
                e.motionY = pck.getMotionY();
                e.motionZ = pck.getMotionZ();
                e.metadata = pck.getMetadata();
                ReListenerCache.entityCache.put(e.entityId, e);
            }
            if (event.getPacket() instanceof ServerSpawnObjectPacket) {
                var pck = (ServerSpawnObjectPacket) event.getPacket();
                EntityObject e = new EntityObject();
                e.type = EntityType.OBJECT;
                e.entityId = pck.getEntityId();
                e.uuid = pck.getUUID();
                e.objectType = pck.getType();
                e.posX = pck.getX();
                e.posY = pck.getY();
                e.posZ = pck.getZ();
                e.pitch = pck.getPitch();
                e.yaw = pck.getYaw();
                e.motionX = pck.getMotionX();
                e.motionY = pck.getMotionY();
                e.motionZ = pck.getMotionZ();
                e.data = pck.getData();
                ReListenerCache.entityCache.put(e.entityId, e);
            }
            if (event.getPacket() instanceof ServerEntityDestroyPacket) {
                var pck = (ServerEntityDestroyPacket) event.getPacket();
                for (int entityId : pck.getEntityIds()) {
                    ReListenerCache.entityCache.remove(entityId);
                }
            }
            if (event.getPacket() instanceof ServerEntityAttachPacket) {
                var pck = (ServerEntityAttachPacket) event.getPacket();
                EntityRotation entityRotation = (EntityRotation) ReListenerCache.entityCache.get(pck.getEntityId());
                if (pck.getAttachedToId() == -1) {
                    entityRotation.isLeashed = false;
                    entityRotation.leashedID = pck.getAttachedToId();
                } else {
                    entityRotation.isLeashed = true;
                    entityRotation.leashedID = pck.getAttachedToId();
                }
            }
            if (event.getPacket() instanceof ServerEntityCollectItemPacket) {
                var pck = (ServerEntityCollectItemPacket) event.getPacket();
                ReListenerCache.entityCache.remove(pck.getCollectedEntityId());
            }
            if (event.getPacket() instanceof ServerEntityEffectPacket) {
                var pck = (ServerEntityEffectPacket) event.getPacket();
                PotionEffect effect = new PotionEffect();
                effect.effect = pck.getEffect();
                effect.amplifier = pck.getAmplifier();
                effect.duration = pck.getDuration();
                effect.ambient = pck.isAmbient();
                effect.showParticles = pck.getShowParticles();
                ((EntityEquipment) ReListenerCache.entityCache.get(pck.getEntityId())).potionEffects.add(effect);
            }
            if (event.getPacket() instanceof ServerEntityEquipmentPacket) {
                var pck = (ServerEntityEquipmentPacket) event.getPacket();
                Entity entity = ReListenerCache.entityCache.get(pck.getEntityId());
                if (entity instanceof EntityObject) {
                    ReMinecraft.INSTANCE.logger.logError("Server tried adding equipment to an EntityObject! Ignoring.");
                    return;
                }
                EntityEquipment equipment = (EntityEquipment) entity;
                equipment.equipment.put(pck.getSlot(), pck.getItem());
            }
            if (event.getPacket() instanceof ServerEntityHeadLookPacket) {
                var pck = (ServerEntityHeadLookPacket) event.getPacket();
                EntityRotation e = (EntityRotation) ReListenerCache.entityCache.get(pck.getEntityId());
                if (e == null) {
                    ReMinecraft.INSTANCE.logger.logError
                            ("Null entity with entity id " + pck.getEntityId());
                    ReMinecraft.INSTANCE.childClients.stream()
                            .filter(ChildReClient::isPlaying)
                            .forEach(client -> client.getSession().send(event.getPacket()));
                    return;
                }
                e.headYaw = pck.getHeadYaw();
            }
            if (event.getPacket() instanceof ServerEntityMovementPacket) {
                var pck = (ServerEntityMovementPacket) event.getPacket();
                Entity e = ReListenerCache.entityCache.get(pck.getEntityId());
                if (e == null) {
                    ReMinecraft.INSTANCE.logger.logError
                            ("Null entity with entity id " + pck.getEntityId());
                    ReMinecraft.INSTANCE.childClients.stream()
                            .filter(ChildReClient::isPlaying)
                            .forEach(client -> client.getSession().send(event.getPacket()));
                    return;
                }
                e.posX += pck.getMovementX() / 4096d;
                e.posY += pck.getMovementY() / 4096d;
                e.posZ += pck.getMovementZ() / 4096d;
                boolean flag;
                var field = ServerEntityMovementPacket.class.getDeclaredField("rot");
                field.setAccessible(true); // leet hax
                flag = (boolean) field.get(pck);
                if (flag && e instanceof EntityRotation) {
                    ((EntityRotation) e).yaw = pck.getYaw();
                    ((EntityRotation) e).pitch = pck.getPitch();
                }
            }
            if (event.getPacket() instanceof ServerEntityPropertiesPacket) {
                var pck = (ServerEntityPropertiesPacket) event.getPacket();
                EntityRotation rotation = (EntityRotation) ReListenerCache.entityCache.get(pck.getEntityId());
                rotation.properties.addAll(pck.getAttributes());
            }
            if (event.getPacket() instanceof ServerEntityRemoveEffectPacket) {
                var pck = (ServerEntityRemoveEffectPacket) event.getPacket();
                EntityEquipment e = (EntityEquipment) ReListenerCache.entityCache.get(pck.getEntityId());
                e.potionEffects.remove(pck.getEffect());
            }
            if (event.getPacket() instanceof ServerEntitySetPassengersPacket) {
                var pck = (ServerEntitySetPassengersPacket) event.getPacket();
                EntityEquipment equipment = (EntityEquipment) ReListenerCache.entityCache.get(pck.getEntityId());
                if (pck.getPassengerIds() == null || pck.getPassengerIds().length == 0) {
                    equipment.passengerIds = null;
                } else {
                    equipment.passengerIds = pck.getPassengerIds();
                }
            }
            if (event.getPacket() instanceof ServerEntityTeleportPacket) {
                var pck = (ServerEntityTeleportPacket) event.getPacket();
                Entity entity = ReListenerCache.entityCache.get(pck.getEntityId());
                if (entity == null) {
                    ReMinecraft.INSTANCE.logger.logError
                            ("Null entity with entity id " + pck.getEntityId());
                    this.sendToChildren(event.getPacket());
                    return;
                }
                entity.posX = pck.getX();
                entity.posY = pck.getY();
                entity.posZ = pck.getZ();
                if (entity instanceof EntityRotation) {
                    ((EntityRotation) entity).yaw = pck.getYaw();
                    ((EntityRotation) entity).pitch = pck.getPitch();
                }
            }
            if (event.getPacket() instanceof ServerVehicleMovePacket) {
                var pck = (ServerVehicleMovePacket) event.getPacket();
                Entity entity = Entity.getEntityBeingRiddenBy(ReListenerCache.entityId);
                if (entity == null) {
                    return;
                }
                entity.posX = pck.getX();
                entity.posY = pck.getY();
                entity.posZ = pck.getZ();
                if (entity instanceof EntityRotation) {
                    ((EntityRotation) entity).yaw = pck.getYaw();
                    ((EntityRotation) entity).pitch = pck.getPitch();
                }
            }
            sendToChildren(event.getPacket());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToChildren(Packet pck) {
        ReMinecraft.INSTANCE.childClients.stream()
                .filter(ChildReClient::isPlaying)
                .forEach(client -> client.getSession().send(pck));
    }

    /**
     * Invoked when a packet prepares to be sent
     */
    @Override
    public void packetSending(PacketSendingEvent packetSendingEvent) {

    }

    /**
     * Invoked when a packet has been successfully dispatched
     */
    @Override
    public void packetSent(PacketSentEvent packetSentEvent) {

    }

    /**
     * Invoked when the client connects to the remote server
     */
    @Override
    public void connected(ConnectedEvent connectedEvent) {
        ReMinecraft.INSTANCE.logger.log
                ("Connected to " + connectedEvent.getSession().getHost() +
                        ":"
                        + connectedEvent.getSession().getPort());
    }

    /**
     * Invoked when the client is disconnecting from the remote server
     */
    @Override
    public void disconnecting(DisconnectingEvent disconnectingEvent) {

    }

    /**
     * Invoked when the client has been disconnected from the remote server
     * =
     */
    @Override
    public void disconnected(DisconnectedEvent disconnectedEvent) {
        ReMinecraft.INSTANCE.logger.logWarning("Disconnected: " + disconnectedEvent.getReason());
    }

    /**
     * For caching importing information, like chunks and inventory data
     */
    public static class ReListenerCache {
        /**
         * Player object
         */
        public static EntityPlayer player;
        /**
         * Player inventory
         */
        public static ServerWindowItemsPacket playerInventory;
        /**
         * Player position
         */
        public static double posX = 0;
        public static double posY = 0;
        public static double posZ = 0;
        public static float yaw = 0;
        public static float pitch = 0;
        public static boolean onGround;
        public static int dimension = 0;
        /**
         * Player entity ID
         */
        public static int entityId = 0;
        public static GameMode gameMode = GameMode.SURVIVAL;
        public static UUID uuid;
        public static float health;
        public static int food;
        public static float saturation;
        /**
         * Needed caches
         */
        public static ConcurrentHashMap<Long, Column> chunkCache = new ConcurrentHashMap<>();
        public static ConcurrentHashMap<Integer, Entity> entityCache = new ConcurrentHashMap<>();
        public static HashMap<UUID, ServerBossBarPacket> cachedBossBars = new HashMap<>();
        public static BufferedImage icon = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        public static int messagesRecieved = 0;
        /**
         * Tablist header/footer
         */
        public static Message tabHeader;
        public static Message tabFooter;
        public static List<PlayerListEntry> playerListEntries = new ArrayList<>();
    }
}
