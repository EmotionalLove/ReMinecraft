package com.sasha.reminecraft.client;

import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.ItemStack;
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.packet.ingame.server.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerChangeHeldItemPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerHealthPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnMobPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnObjectPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerWindowItemsPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.*;
import com.github.steveice10.mc.protocol.packet.login.server.LoginDisconnectPacket;
import com.github.steveice10.mc.protocol.packet.login.server.LoginSuccessPacket;
import com.github.steveice10.packetlib.event.session.*;
import com.github.steveice10.packetlib.packet.Packet;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.event.RemoteServerPacketRecieveEvent;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.reaction.client.*;
import com.sasha.reminecraft.server.ReServerManager;
import com.sasha.reminecraft.util.entity.Entity;
import com.sasha.reminecraft.util.entity.EntityPlayer;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens and processes packets being Tx'd and Rx'd from the remote server.
 */
public class ReClient implements SessionListener {

    private LinkedHashMap<Class<? extends Packet>, IPacketReactor<?>> reactionRegistry = new LinkedHashMap<>();

    public ReClient() {
        this.reactionRegistry.put(LoginDisconnectPacket.class, new LoginDisconnectReaction());
        this.reactionRegistry.put(LoginSuccessPacket.class, new LoginSuccessReaction());
        this.reactionRegistry.put(ServerBlockChangePacket.class, new ServerBlockChangeReaction());
        this.reactionRegistry.put(ServerChatPacket.class, new ServerChatReaction());
        this.reactionRegistry.put(ServerChunkDataPacket.class, new ServerChunkDataReaction());
        this.reactionRegistry.put(ServerEntityAttachPacket.class, new ServerEntityAttachReaction());
        this.reactionRegistry.put(ServerEntityCollectItemPacket.class, new ServerEntityCollectItemReaction());
        this.reactionRegistry.put(ServerEntityDestroyPacket.class, new ServerEntityDestroyReaction());
        this.reactionRegistry.put(ServerEntityEffectPacket.class, new ServerEntityEffectReaction());
        this.reactionRegistry.put(ServerEntityEquipmentPacket.class, new ServerEntityEquipmentReaction());
        this.reactionRegistry.put(ServerEntityHeadLookPacket.class, new ServerEntityHeadLookReaction());
        this.reactionRegistry.put(ServerEntityMovementPacket.class, new ServerEntityMovementReaction());
        this.reactionRegistry.put(ServerEntityPropertiesPacket.class, new ServerEntityPropertiesReaction());
        this.reactionRegistry.put(ServerEntityRemoveEffectPacket.class, new ServerEntityRemoveEffectReaction());
        this.reactionRegistry.put(ServerEntitySetPassengersPacket.class, new ServerEntitySetPassengersReaction());
        this.reactionRegistry.put(ServerEntityTeleportPacket.class, new ServerEntityTeleportReaction());
        this.reactionRegistry.put(ServerJoinGamePacket.class, new ServerJoinGameReaction());
        this.reactionRegistry.put(ServerMultiBlockChangePacket.class, new ServerMultiBlockChangeReaction());
        this.reactionRegistry.put(ServerNotifyClientPacket.class, new ServerNotifyClientReaction());
        this.reactionRegistry.put(ServerPlayerChangeHeldItemPacket.class, new ServerPlayerChangeHeldItemReaction());
        this.reactionRegistry.put(ServerPlayerHealthPacket.class, new ServerPlayerHealthReaction());
        this.reactionRegistry.put(ServerPlayerListDataPacket.class, new ServerPlayerListDataReaction());
        this.reactionRegistry.put(ServerPlayerListEntryPacket.class, new ServerPlayerListEntryReaction());
        this.reactionRegistry.put(ServerRespawnPacket.class, new ServerRespawnReaction());
        this.reactionRegistry.put(ServerSpawnMobPacket.class, new ServerSpawnMobReaction());
        this.reactionRegistry.put(ServerSpawnObjectPacket.class, new ServerSpawnObjectReaction());
        this.reactionRegistry.put(ServerUnloadChunkPacket.class, new ServerUnloadChunkReaction());
        this.reactionRegistry.put(ServerUnlockRecipesPacket.class, new ServerUnlockRecipesReaction());
        this.reactionRegistry.put(ServerVehicleMovePacket.class, new ServerVehicleMoveReaction());
        this.reactionRegistry.put(ServerWindowItemsPacket.class, new ServerWindowItemsReaction());
        this.reactionRegistry.put(ServerPlayerPositionRotationPacket.class, new ServerPlayerPositionRotationReaction());
        this.reactionRegistry.put(ServerSpawnPlayerPacket.class, new ServerSpawnPlayerReaction());
    }

    /**
     * Invoked when a packet is recieved
     */
    @Override
    public void packetReceived(PacketReceivedEvent ev) {
        RemoteServerPacketRecieveEvent event = new RemoteServerPacketRecieveEvent(ev.getPacket());
        ReMinecraft.INSTANCE.EVENT_BUS.invokeEvent(event);
        if (event.isCancelled()) return;
        try {
            if (!reactionRegistry.containsKey(ev.getPacket().getClass())) { // so we aren't blocking packets that dont need special processing
                ReMinecraft.INSTANCE.sendToChildren(event.getRecievedPacket());
                return;
            }
            this.reactionRegistry.forEach((pck, reactor) -> { // iterate over the registered reactions
                if (pck == ev.getPacket().getClass()) { // if the reaction is paired with pck's clas
                    boolean flag = reactor.takeAction(ev.getPacket());
                    if (flag) // perform the action
                        ReMinecraft.INSTANCE.sendToChildren(event.getRecievedPacket()); // send the packet to children if true
                } //ez
            });
        } catch (Exception e) {
            System.err.println("A severe error occured during a recieved server packet's procesing!");
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
        try {
            ReMinecraft.INSTANCE.minecraftServer = ReServerManager.prepareServer();
            ReMinecraft.INSTANCE.minecraftServer.addListener(new ReServerManager());
            ReMinecraft.INSTANCE.minecraftServer.bind(true);
        } catch (Exception e) {
            e.printStackTrace();
            ReMinecraft.INSTANCE.logger.logError("A severe exception occurred whilst creating the server! Maybe there's already a server running on the port?");
        }
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
