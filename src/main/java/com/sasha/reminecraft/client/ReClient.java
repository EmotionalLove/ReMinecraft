package com.sasha.reminecraft.client;

import com.github.steveice10.mc.protocol.data.game.ClientRequest;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.data.game.UnlockRecipesAction;
import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.ItemStack;
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.github.steveice10.mc.protocol.data.game.world.block.BlockChangeRecord;
import com.github.steveice10.mc.protocol.data.game.world.notify.ClientNotification;
import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientRequestPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.world.ClientTeleportConfirmPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerChangeHeldItemPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerHealthPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnMobPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnObjectPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerPreparedCraftingGridPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerWindowItemsPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.*;
import com.github.steveice10.mc.protocol.packet.login.server.LoginDisconnectPacket;
import com.github.steveice10.mc.protocol.packet.login.server.LoginSuccessPacket;
import com.github.steveice10.packetlib.event.session.*;
import com.google.gson.JsonElement;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.event.ChatRecievedEvent;
import com.sasha.reminecraft.api.event.PlayerDamagedEvent;
import com.sasha.reminecraft.api.event.RemoteServerPacketRecieveEvent;
import com.sasha.reminecraft.api.event.ServerResetPlayerPositionEvent;
import com.sasha.reminecraft.client.children.ChildReClient;
import com.sasha.reminecraft.server.ReServerManager;
import com.sasha.reminecraft.util.ChunkUtil;
import com.sasha.reminecraft.util.entity.*;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens and processes packets being Tx'd and Rx'd from the remote server.
 */
public class ReClient implements SessionListener {

    /**
     * Invoked when a packet is recieved
     */
    @Override
    public void packetReceived(PacketReceivedEvent ev) {
        RemoteServerPacketRecieveEvent event = new RemoteServerPacketRecieveEvent(ev.getPacket());
        ReMinecraft.INSTANCE.EVENT_BUS.invokeEvent(event);
        if (event.isCancelled()) return;
        try {
            if (event.getRecievedPacket() instanceof ServerUnlockRecipesPacket) {
                ServerUnlockRecipesPacket pck = (ServerUnlockRecipesPacket) event.getRecievedPacket();
                ReClientCache.INSTANCE.wasRecipeBookOpened = pck.getOpenCraftingBook();
                ReClientCache.INSTANCE.wasFilteringRecipes = pck.getActivateFiltering();
                switch (pck.getAction()) {
                    case ADD:
                        for (Integer recipe : pck.getRecipes()) {
                            if (ReClientCache.INSTANCE.recipeCache.contains(recipe)) continue;
                            ReClientCache.INSTANCE.recipeCache.add(recipe);
                        }
                        break;
                    case REMOVE:
                        for (Integer recipe : pck.getRecipes()) {
                            if (!ReClientCache.INSTANCE.recipeCache.contains(recipe)) continue;
                            ReClientCache.INSTANCE.recipeCache.remove(recipe);
                        }
                        break;
                    case INIT:
                        for (Integer alreadyKnownRecipe : pck.getAlreadyKnownRecipes()) {
                            if (ReClientCache.INSTANCE.recipeCache.contains(alreadyKnownRecipe)) continue;
                            ReClientCache.INSTANCE.recipeCache.add(alreadyKnownRecipe);
                        }
                        for (Integer recipe : pck.getRecipes()) {
                            if (!ReClientCache.INSTANCE.recipeCache.contains(recipe)) continue;
                            ReClientCache.INSTANCE.recipeCache.remove(recipe);
                        }
                        break;
                    default:
                        for (Integer alreadyKnownRecipe : pck.getAlreadyKnownRecipes()) {
                            if (ReClientCache.INSTANCE.recipeCache.contains(alreadyKnownRecipe)) continue;
                            ReClientCache.INSTANCE.recipeCache.add(alreadyKnownRecipe);
                        }
                        for (Integer recipe : pck.getRecipes()) {
                            if (!ReClientCache.INSTANCE.recipeCache.contains(recipe)) continue;
                            ReClientCache.INSTANCE.recipeCache.remove(recipe);
                        }
                }
            }
            if (event.getRecievedPacket() instanceof ServerChatPacket) {
                ServerChatPacket pck = (ServerChatPacket) event.getRecievedPacket();
                ChatRecievedEvent chatEvent = new ChatRecievedEvent(pck.getMessage().getFullText(), System.currentTimeMillis());
                ReMinecraft.INSTANCE.EVENT_BUS.invokeEvent(chatEvent);
                ReMinecraft.INSTANCE.logger.log("(CHAT) " + ((ServerChatPacket) event.getRecievedPacket()).getMessage().getFullText());
                JsonElement msg = pck.getMessage().toJson();
                ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(Message.fromJson(msg), pck.getType()));
                return;
            }
            if (event.getRecievedPacket() instanceof ServerPlayerHealthPacket) {
                //update player health
                ServerPlayerHealthPacket pck = (ServerPlayerHealthPacket) event.getRecievedPacket();
                PlayerDamagedEvent damagedEvent = new PlayerDamagedEvent(ReClientCache.INSTANCE.health, pck.getHealth());
                ReMinecraft.INSTANCE.EVENT_BUS.invokeEvent(damagedEvent);
                ReClientCache.INSTANCE.health = pck.getHealth();
                ReClientCache.INSTANCE.food = pck.getFood();
                ReClientCache.INSTANCE.saturation = pck.getSaturation();
                if (ReClientCache.INSTANCE.health <= 0.0f) {
                    ReMinecraft.INSTANCE.minecraftClient.getSession().send(new ClientRequestPacket(ClientRequest.RESPAWN));
                }
            }
            if (event.getRecievedPacket() instanceof ServerPlayerListEntryPacket) {
                ServerPlayerListEntryPacket pck = (ServerPlayerListEntryPacket) event.getRecievedPacket();
                switch (pck.getAction()) {
                    case ADD_PLAYER:
                        Arrays.stream(pck.getEntries())
                                .filter(e -> !ReClientCache.INSTANCE.playerListEntries.contains(e))
                                .forEach(entry -> ReClientCache.INSTANCE.playerListEntries.add(entry));
                        break;
                    case REMOVE_PLAYER:
                        List<String> toRemove = new ArrayList<>();
                        List<Integer> removalIndexes = new ArrayList<>();
                        Arrays.stream(pck.getEntries()).forEach(entry -> toRemove.add(entry.getProfile().getId().toString()));
                        ReClientCache.INSTANCE.playerListEntries.forEach(entry -> {
                            if (toRemove.contains(entry.getProfile().getId().toString())) {
                                removalIndexes.add(ReClientCache.INSTANCE.playerListEntries.indexOf(entry));
                            }
                        });
                        removalIndexes.forEach(index -> {
                            ReClientCache.INSTANCE.playerListEntries.remove((int)index);
                        });
                        break;
                    case UPDATE_DISPLAY_NAME:
                        LinkedHashMap<UUID, Message> changeMap = new LinkedHashMap<>();
                        for (PlayerListEntry entry : pck.getEntries()) {
                            changeMap.put(entry.getProfile().getId(), entry.getDisplayName());
                        }
                        changeMap.forEach((id, msg) -> {
                            for (PlayerListEntry playerListEntry : ReClientCache.INSTANCE.playerListEntries) {
                                if (playerListEntry.getProfile().getId().equals(id)) {
                                    try {
                                        Field field = playerListEntry.getClass().getDeclaredField("displayName");
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
                            for (PlayerListEntry playerListEntry : ReClientCache.INSTANCE.playerListEntries) {
                                if (playerListEntry.getProfile().getId().equals(id)) {
                                    try {
                                        Field field = playerListEntry.getClass().getDeclaredField("ping");
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
                        gamemodeMap.forEach((id, gm) -> {
                            for (PlayerListEntry playerListEntry : ReClientCache.INSTANCE.playerListEntries) {
                                if (playerListEntry.getProfile().getId().equals(id)) {
                                    try {
                                        Field field = playerListEntry.getClass().getDeclaredField("gameMode");
                                        field.setAccessible(true);
                                        field.set(playerListEntry, gm);
                                    } catch (NoSuchFieldException | IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                        break;
                    default:
                        ReMinecraft.INSTANCE.logger.logError("Unsupported tablist action!");

                }
            }
            if (event.getRecievedPacket() instanceof ServerPlayerListDataPacket) {
                ReClientCache.INSTANCE.tabHeader = ((ServerPlayerListDataPacket) event.getRecievedPacket()).getHeader();
                ReClientCache.INSTANCE.tabFooter = ((ServerPlayerListDataPacket) event.getRecievedPacket()).getFooter();
            }
            if (event.getRecievedPacket() instanceof ServerPlayerPositionRotationPacket) {
                ServerPlayerPositionRotationPacket pck = (ServerPlayerPositionRotationPacket) event.getRecievedPacket();
                ServerResetPlayerPositionEvent resetEvent = new ServerResetPlayerPositionEvent(pck.getX(), pck.getY(), pck.getZ(), pck.getYaw(), pck.getPitch());
                ReMinecraft.INSTANCE.EVENT_BUS.invokeEvent(resetEvent);
                ReClientCache.INSTANCE.posX = pck.getX();
                ReClientCache.INSTANCE.posY = pck.getY();
                ReClientCache.INSTANCE.posZ = pck.getZ();
                ReClientCache.INSTANCE.pitch = pck.getPitch();
                ReClientCache.INSTANCE.yaw = pck.getYaw();
                if (!ReMinecraft.INSTANCE.areChildrenConnected()) {
                    // the notchian client will do this for us, if one is connected
                    ReMinecraft.INSTANCE.minecraftClient.getSession().send(new ClientTeleportConfirmPacket(pck.getTeleportId()));
                }
            }
            if (event.getRecievedPacket() instanceof ServerChunkDataPacket) {
                // VERY IMPORTANT: Chunks will NOT RENDER correctly and be invisible on notchian clients if we
                // do not actually push them correctly. This is apparent with big chunks and newly generated ones
                // that need to be dispersed over multiple packets. Trust me, it's really gay.
                // btw i love phi <33333333333333333 hes like super nice
                ServerChunkDataPacket pck = (ServerChunkDataPacket) event.getRecievedPacket();
                Column column = pck.getColumn();
                long hash = ChunkUtil.getChunkHashFromXZ(column.getX(), column.getZ());
                if (!column.hasBiomeData()) {
                    // if the chunk is thicc or newly generated
                    if (ReClientCache.INSTANCE.chunkCache.containsKey(hash)) {
                        Column chunkToAddTo = ReClientCache.INSTANCE.chunkCache.get(hash);
                        ReMinecraft.INSTANCE.sendToChildren(new ServerUnloadChunkPacket(chunkToAddTo.getX(), chunkToAddTo.getZ()));
                        for (int i = 0; i <= 15; i++) {
                            if (column.getChunks()[i] != null) {
                                chunkToAddTo.getChunks()[i] = column.getChunks()[i];
                            }
                        }
                        ReClientCache.INSTANCE.chunkCache.put(hash, chunkToAddTo);
                        ReMinecraft.INSTANCE.sendToChildren(new ServerChunkDataPacket(chunkToAddTo));
                    }
                } else {
                    ReClientCache.INSTANCE.chunkCache.put(hash, pck.getColumn());
                }
            }
            if (event.getRecievedPacket() instanceof ServerUnloadChunkPacket) {
                ServerUnloadChunkPacket pck = (ServerUnloadChunkPacket) event.getRecievedPacket();
                long hash = ChunkUtil.getChunkHashFromXZ(pck.getX(), pck.getZ());
                ReClientCache.INSTANCE.chunkCache.remove(hash);
            }
            if (event.getRecievedPacket() instanceof ServerWindowItemsPacket) {
                ServerWindowItemsPacket pck = (ServerWindowItemsPacket) event.getRecievedPacket();
                if (pck.getWindowId() == 0) {
                    ReClientCache.INSTANCE.playerInventory = pck.getItems();
                }
            }
            if (event.getRecievedPacket() instanceof ServerBlockChangePacket) {
                // update the cached chunks
                // sometimes inaccurate (i think?)
                ServerBlockChangePacket pck = (ServerBlockChangePacket) event.getRecievedPacket();
                int chunkX = pck.getRecord().getPosition().getX() >> 4;
                int chunkZ = pck.getRecord().getPosition().getZ() >> 4;
                int cubeY = ChunkUtil.clamp(pck.getRecord().getPosition().getY() >> 4, 0, 15);
                Column column = ReClientCache.INSTANCE.chunkCache
                        .getOrDefault(ChunkUtil.getChunkHashFromXZ(chunkX, chunkZ), null);
                if (column == null) {
                    // not ignoring this can leak memory in the notchian client
                    ReMinecraft.INSTANCE.logger.logDebug("Ignoring server request to change blocks in an unloaded chunk, is the remote server running a modified Minecraft server jar? This could cause issues.");
                    return;
                }
                Chunk subChunk = column.getChunks()[cubeY];
                int cubeRelY = Math.abs(pck.getRecord().getPosition().getY() - 16 * cubeY);
                try {
                    subChunk.getBlocks().set(Math.abs(Math.abs(pck.getRecord().getPosition().getX()) - (Math.abs(Math.abs(pck.getRecord().getPosition().getX() >> 4)) * 16)), ChunkUtil.clamp(cubeRelY, 0, 15), Math.abs(Math.abs(pck.getRecord().getPosition().getZ()) - (Math.abs(Math.abs(pck.getRecord().getPosition().getZ() >> 4)) * 16)), pck.getRecord().getBlock());
                    column.getChunks()[cubeY] = subChunk;
                    ReClientCache.INSTANCE.chunkCache.put(ChunkUtil.getChunkHashFromXZ(chunkX, chunkZ), column);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ReClientCache.INSTANCE.chunkCache.put(ChunkUtil.getChunkHashFromXZ(chunkX, chunkZ), column);
            }
            if (event.getRecievedPacket() instanceof ServerMultiBlockChangePacket) {
                // this is more complicated
                ServerMultiBlockChangePacket pck = (ServerMultiBlockChangePacket) event.getRecievedPacket();
                int chunkX = pck.getRecords()[0].getPosition().getX() >> 4;
                int chunkZ = pck.getRecords()[0].getPosition().getZ() >> 4;
                Column column = ReClientCache.INSTANCE.chunkCache.getOrDefault(ChunkUtil.getChunkHashFromXZ(chunkX, chunkZ), null);
                if (column == null) {
                    // not ignoring this can leak memory in the notchian client
                    ReMinecraft.INSTANCE.logger.logDebug("Ignoring server request to change blocks in an unloaded chunk, is the remote server running a modified Minecraft server jar? This could cause issues.");
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
                ReClientCache.INSTANCE.chunkCache.put(ChunkUtil.getChunkHashFromXZ(chunkX, chunkZ), column);
            }
            if (event.getRecievedPacket() instanceof ServerJoinGamePacket) {
                // this is when YOU join the server, not another player
                ServerJoinGamePacket pck = (ServerJoinGamePacket) event.getRecievedPacket();
                ReClientCache.INSTANCE.dimension = pck.getDimension();
                ReClientCache.INSTANCE.entityId = pck.getEntityId();
                ReClientCache.INSTANCE.gameMode = pck.getGameMode();
                EntityPlayer player = new EntityPlayer();
                player.type = EntityType.REAL_PLAYER;
                player.entityId = ReClientCache.INSTANCE.entityId;
                player.uuid = ReClientCache.INSTANCE.uuid;
                ReClientCache.INSTANCE.player = player;
                ReClientCache.INSTANCE.entityCache.put(player.entityId, player);
            }
            if (event.getRecievedPacket() instanceof LoginSuccessPacket) {
                ReClientCache.INSTANCE.uuid = ((LoginSuccessPacket) event.getRecievedPacket()).getProfile().getId();
            }
            if (event.getRecievedPacket() instanceof ServerNotifyClientPacket) {
                ServerNotifyClientPacket pck = (ServerNotifyClientPacket) event.getRecievedPacket();
                if (pck.getNotification() == ClientNotification.CHANGE_GAMEMODE) {
                    ReClientCache.INSTANCE.gameMode = (GameMode) pck.getValue();
                }
            }
            if (event.getRecievedPacket() instanceof ServerRespawnPacket) {
                // clear everything because none of it matters now :smiling_imp:
                ServerRespawnPacket pck = (ServerRespawnPacket) event.getRecievedPacket();
                ReClientCache.INSTANCE.dimension = pck.getDimension();
                ReClientCache.INSTANCE.gameMode = pck.getGameMode();
                ReClientCache.INSTANCE.chunkCache.clear();
                ReClientCache.INSTANCE.entityCache.entrySet().removeIf(integerEntityEntry -> integerEntityEntry.getKey() != ReClientCache.INSTANCE.entityId);
                ReClientCache.INSTANCE.cachedBossBars.clear();
                ReClientCache.INSTANCE.player.potionEffects.clear();
            }
            if (event.getRecievedPacket() instanceof LoginDisconnectPacket) {
                LoginDisconnectPacket pck = (LoginDisconnectPacket) event.getRecievedPacket();
                ReMinecraft.INSTANCE.logger.logError("Kicked whilst logging in: " + pck.getReason().getFullText());
                ReMinecraft.INSTANCE.minecraftClient.getSession().disconnect(pck.getReason().getFullText(), true);
            }
            if (event.getRecievedPacket() instanceof ServerSpawnMobPacket) {
                ServerSpawnMobPacket pck = (ServerSpawnMobPacket) event.getRecievedPacket();
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
                ReClientCache.INSTANCE.entityCache.put(e.entityId, e);
            }
            if (event.getRecievedPacket() instanceof ServerSpawnObjectPacket) {
                ServerSpawnObjectPacket pck = (ServerSpawnObjectPacket) event.getRecievedPacket();
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
                ReClientCache.INSTANCE.entityCache.put(e.entityId, e);
            }
            if (event.getRecievedPacket() instanceof ServerEntityDestroyPacket) {
                ServerEntityDestroyPacket pck = (ServerEntityDestroyPacket) event.getRecievedPacket();
                for (int entityId : pck.getEntityIds()) {
                    ReClientCache.INSTANCE.entityCache.remove(entityId);
                }
            }
            if (event.getRecievedPacket() instanceof ServerEntityAttachPacket) {
                ServerEntityAttachPacket pck = (ServerEntityAttachPacket) event.getRecievedPacket();
                EntityRotation entityRotation = (EntityRotation) ReClientCache.INSTANCE.entityCache.get(pck.getEntityId());
                if (pck.getAttachedToId() == -1) {
                    entityRotation.isLeashed = false;
                    entityRotation.leashedID = pck.getAttachedToId();
                } else {
                    entityRotation.isLeashed = true;
                    entityRotation.leashedID = pck.getAttachedToId();
                }
            }
            if (event.getRecievedPacket() instanceof ServerEntityCollectItemPacket) {
                ServerEntityCollectItemPacket pck = (ServerEntityCollectItemPacket) event.getRecievedPacket();
                ReClientCache.INSTANCE.entityCache.remove(pck.getCollectedEntityId());
            }
            if (event.getRecievedPacket() instanceof ServerEntityEffectPacket) {
                ServerEntityEffectPacket pck = (ServerEntityEffectPacket) event.getRecievedPacket();
                PotionEffect effect = new PotionEffect();
                effect.effect = pck.getEffect();
                effect.amplifier = pck.getAmplifier();
                effect.duration = pck.getDuration();
                effect.ambient = pck.isAmbient();
                effect.showParticles = pck.getShowParticles();
                ((EntityEquipment) ReClientCache.INSTANCE.entityCache.get(pck.getEntityId())).potionEffects.add(effect);
            }
            if (event.getRecievedPacket() instanceof ServerEntityEquipmentPacket) {
                ServerEntityEquipmentPacket pck = (ServerEntityEquipmentPacket) event.getRecievedPacket();
                Entity entity = ReClientCache.INSTANCE.entityCache.get(pck.getEntityId());
                if (entity instanceof EntityObject) {
                    ReMinecraft.INSTANCE.logger.logError("Server tried adding equipment to an EntityObject! Ignoring.");
                    return;
                }
                EntityEquipment equipment = (EntityEquipment) entity;
                equipment.equipment.put(pck.getSlot(), pck.getItem());
            }
            if (event.getRecievedPacket() instanceof ServerEntityHeadLookPacket) {
                ServerEntityHeadLookPacket pck = (ServerEntityHeadLookPacket) event.getRecievedPacket();
                EntityRotation e = (EntityRotation) ReClientCache.INSTANCE.entityCache.get(pck.getEntityId());
                if (e == null) {
                    ReMinecraft.INSTANCE.logger.logDebug
                            ("Null entity with entity id " + pck.getEntityId());
                    ReMinecraft.INSTANCE.sendToChildren(event.getRecievedPacket());
                    return;
                }
                e.headYaw = pck.getHeadYaw();
            }
            if (event.getRecievedPacket() instanceof ServerEntityMovementPacket) {
                ServerEntityMovementPacket pck = (ServerEntityMovementPacket) event.getRecievedPacket();
                Entity e = ReClientCache.INSTANCE.entityCache.get(pck.getEntityId());
                if (e == null) {
                    ReMinecraft.INSTANCE.logger.logDebug
                            ("Null entity with entity id " + pck.getEntityId());
                    ReMinecraft.INSTANCE.sendToChildren(event.getRecievedPacket());
                    return;
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
            }
            if (event.getRecievedPacket() instanceof ServerEntityPropertiesPacket) {
                ServerEntityPropertiesPacket pck = (ServerEntityPropertiesPacket) event.getRecievedPacket();
                EntityRotation rotation = (EntityRotation) ReClientCache.INSTANCE.entityCache.get(pck.getEntityId());
                if (rotation == null) {
                    ReMinecraft.INSTANCE.sendToChildren(event.getRecievedPacket());
                    return;
                }
                rotation.properties.addAll(pck.getAttributes());
            }
            if (event.getRecievedPacket() instanceof ServerEntityRemoveEffectPacket) {
                ServerEntityRemoveEffectPacket pck = (ServerEntityRemoveEffectPacket) event.getRecievedPacket();
                EntityEquipment e = (EntityEquipment) ReClientCache.INSTANCE.entityCache.get(pck.getEntityId());
                e.potionEffects.remove(pck.getEffect());
            }
            if (event.getRecievedPacket() instanceof ServerEntitySetPassengersPacket) {
                ServerEntitySetPassengersPacket pck = (ServerEntitySetPassengersPacket) event.getRecievedPacket();
                EntityEquipment equipment = (EntityEquipment) ReClientCache.INSTANCE.entityCache.get(pck.getEntityId());
                if (pck.getPassengerIds() == null || pck.getPassengerIds().length == 0) {
                    equipment.passengerIds = null;
                } else {
                    equipment.passengerIds = pck.getPassengerIds();
                }
            }
            if (event.getRecievedPacket() instanceof ServerEntityTeleportPacket) {
                ServerEntityTeleportPacket pck = (ServerEntityTeleportPacket) event.getRecievedPacket();
                Entity entity = ReClientCache.INSTANCE.entityCache.get(pck.getEntityId());
                if (entity == null) {
                    ReMinecraft.INSTANCE.logger.logDebug
                            ("Null entity with entity id " + pck.getEntityId());
                    ReMinecraft.INSTANCE.childClients.stream()
                            .filter(ChildReClient::isPlaying)
                            .forEach(client -> client.getSession().send(event.getRecievedPacket()));
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
            if (event.getRecievedPacket() instanceof ServerVehicleMovePacket) {
                ServerVehicleMovePacket pck = (ServerVehicleMovePacket) event.getRecievedPacket();
                Entity entity = Entity.getEntityBeingRiddenBy(ReClientCache.INSTANCE.entityId);
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
            if (event.getRecievedPacket() instanceof ServerPlayerChangeHeldItemPacket) {
                ServerPlayerChangeHeldItemPacket pck = (ServerPlayerChangeHeldItemPacket) event.getRecievedPacket();
                ReClientCache.INSTANCE.heldItem = pck.getSlot();
            }
            ReMinecraft.INSTANCE.sendToChildren(event.getRecievedPacket());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        ReMinecraft.INSTANCE.logger.log("Starting server on " + ReMinecraft.INSTANCE.MAIN_CONFIG.var_hostServerIp + ":" +
                ReMinecraft.INSTANCE.MAIN_CONFIG.var_hostServerPort);
        ReMinecraft.INSTANCE.minecraftServer = ReServerManager.prepareServer();
        ReMinecraft.INSTANCE.minecraftServer.addListener(new ReServerManager());
        ReMinecraft.INSTANCE.minecraftServer.bind(true);
        ReMinecraft.INSTANCE.logger.log("Server started!");
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
        ReMinecraft.INSTANCE.minecraftServer.close(true);
        ReMinecraft.INSTANCE.logger.logWarning("Disconnected: " + disconnectedEvent.getReason());
        ReMinecraft.INSTANCE.reLaunch();
    }

    /**
     * For caching importing information, like chunks and inventory data
     */
    public static class ReClientCache {

        public static ReClientCache INSTANCE;

        public String playerName;
        public UUID playerUuid;
        /**
         * Player object
         */
        public EntityPlayer player;
        /**
         * Player inventory
         */
        public ItemStack[] playerInventory;
        public boolean wasFilteringRecipes;
        public boolean wasRecipeBookOpened;
        public List<Integer> recipeCache = new ArrayList<>();
        public int heldItem = 0;
        /**
         * Player position
         */
        public double posX = 0;
        public double posY = 0;
        public double posZ = 0;
        public float yaw = 0;
        public float pitch = 0;
        public boolean onGround;
        public int dimension = 0;
        /**
         * Player entity ID
         */
        public int entityId = 0;
        public GameMode gameMode = GameMode.SURVIVAL;
        public UUID uuid;
        public float health = 20f;
        public int food;
        public float saturation;
        /**
         * Needed caches
         */
        public ConcurrentHashMap<Long, Column> chunkCache = new ConcurrentHashMap<>();
        public ConcurrentHashMap<Integer, Entity> entityCache = new ConcurrentHashMap<>();
        public HashMap<UUID, ServerBossBarPacket> cachedBossBars = new HashMap<>();
        public BufferedImage icon = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        public int messagesRecieved = 0;
        /**
         * Tablist header/footer
         */
        public Message tabHeader = Message.fromString("\n\2477RE:Minecraft \247d" + ReMinecraft.VERSION + "\n");
        public Message tabFooter = Message.fromString("\n\2477Created by Sasha\nhttps://github.com/EmotionalLove/ReMinecraft\n");
        public List<PlayerListEntry> playerListEntries = new ArrayList<>();

        public ReClientCache() {
            INSTANCE = this;
        }


    }
}
