package com.sasha.reminecraft.client;

import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.packet.ingame.client.world.ClientTeleportConfirmPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerBossBarPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPlayerListDataPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPlayerListEntryPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerHealthPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerWindowItemsPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerBlockChangePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerUnloadChunkPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerUpdateTimePacket;
import com.github.steveice10.packetlib.event.session.*;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.util.ChunkUtil;
import com.sasha.reminecraft.util.entity.Entity;
import com.sasha.reminecraft.util.entity.EntityPlayer;

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
                ReListenerCache.health = ((ServerPlayerHealthPacket) event.getPacket()).getHealth();
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
                                    ReListenerCache.playerListEntries.remove(playerListEntry);
                                    break;
                                }
                            }
                        });
                        ReListenerCache.playerListEntries.addAll(Arrays.asList(pck.getEntries()));
                        break;
                    case UPDATE_LATENCY:
                        LinkedHashMap<UUID, Integer> pingMap = new LinkedHashMap<>();
                        for (PlayerListEntry entry : pck.getEntries()) {
                            pingMap.put(entry.getProfile().getId(), entry.getPing());
                        }
                        pingMap.forEach((id, ping) -> {
                            for (PlayerListEntry playerListEntry : ReListenerCache.playerListEntries) {
                                if (playerListEntry.getProfile().getId().equals(id)) {
                                    ReListenerCache.playerListEntries.remove(playerListEntry);
                                    break;
                                }
                            }
                        });
                        ReListenerCache.playerListEntries.addAll(Arrays.asList(pck.getEntries()));
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
                        // todo: UNLOAD chunkToAddTo
                        for (int i = 0; i <= 15; i++) {
                            if (column.getChunks()[i] != null) {
                                chunkToAddTo.getChunks()[i] = column.getChunks()[i];
                            }
                        }
                        ReListenerCache.chunkCache.put(hash, chunkToAddTo);
                        // todo: LOAD chunkToAddTo
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
                    ReMinecraft.INSTANCE.logger.logWarning("Ignoring request to change blocks in an unloaded chunk, is the remote server running a modified Minecraft server jar?");
                    return;
                }
                Chunk subChunk = column.getChunks()[cubeY];
                int cubeRelY = Math.abs(pck.getRecord().getPosition().getY() - 16 * cubeY);
                try {
                    subChunk.getBlocks().set(Math.abs(Math.abs(pck.getRecord().getPosition().getX()) - (Math.abs(Math.abs(pck.getRecord().getPosition().getX() >> 4)) * 16)), ChunkUtil.clamp(cubeRelY, 0, 15), Math.abs(Math.abs(pck.getRecord().getPosition().getZ()) - (Math.abs(Math.abs(pck.getRecord().getPosition().getZ() >> 4)) * 16)), pck.getRecord().getBlock());
                    column.getChunks()[cubeY] = subChunk;
                    ReListenerCache.chunkCache.put(ChunkUtil.getChunkHashFromXZ(chunkX, chunkZ), column);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                ReListenerCache.chunkCache.put(ChunkUtil.getChunkHashFromXZ(chunkX, chunkZ), column);
            }
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
