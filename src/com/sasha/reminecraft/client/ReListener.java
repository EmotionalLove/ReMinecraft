package com.sasha.reminecraft.client;

import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerBossBarPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerWindowItemsPacket;
import com.github.steveice10.packetlib.event.session.*;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.util.entity.Entity;
import com.sasha.reminecraft.util.entity.EntityPlayer;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens and processes packets being Tx'd and Rx'd from the remote server.
 */
public class ReListener implements SessionListener {

    /**
     * Invoked when a packet is recieved
     */
    @Override
    public void packetReceived(PacketReceivedEvent packetReceivedEvent) {
        if (packetReceivedEvent.getPacket() instanceof ServerChatPacket) {
            ReMinecraft.INSTANCE.logger.log(((ServerChatPacket) packetReceivedEvent.getPacket()).getMessage().getFullText());
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
        public static String tabHeader;
        public static String tabFooter;
    }
}
