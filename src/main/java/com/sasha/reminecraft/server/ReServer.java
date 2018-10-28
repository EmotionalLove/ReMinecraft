package com.sasha.reminecraft.server;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntryAction;
import com.github.steveice10.mc.protocol.data.game.entity.EquipmentSlot;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.ItemStack;
import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientKeepAlivePacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientCraftingBookDataPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientPrepareCraftingGridPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerChangeHeldItemPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerHealthPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnMobPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnObjectPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerWindowItemsPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import com.github.steveice10.mc.protocol.packet.login.client.LoginStartPacket;
import com.github.steveice10.mc.protocol.packet.login.server.LoginDisconnectPacket;
import com.github.steveice10.mc.protocol.packet.login.server.LoginSuccessPacket;
import com.github.steveice10.packetlib.event.session.*;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.event.ChildServerPacketRecieveEvent;
import com.sasha.reminecraft.api.event.ChildServerPacketSendEvent;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.client.children.ChildReClient;
import com.sasha.reminecraft.util.entity.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class ReServer extends SessionAdapter {

    private ChildReClient child;

    public ReServer(ChildReClient child) {
        this.child = child;
    }

    /**
     * Invoked when the child sends us a packet
     */
    @Override
    public void packetReceived(PacketReceivedEvent ev) {
        ChildServerPacketRecieveEvent event = new ChildServerPacketRecieveEvent(this.child, ev.getPacket());
        ReMinecraft.INSTANCE.EVENT_BUS.invokeEvent(event);
        MinecraftProtocol protocol = (MinecraftProtocol) child.getSession().getPacketProtocol();
        if (event.getRecievedPacket() instanceof LoginStartPacket && (protocol.getSubProtocol() == SubProtocol.LOGIN || protocol.getSubProtocol() == SubProtocol.HANDSHAKE)) {
            LoginStartPacket pck = (LoginStartPacket) event.getRecievedPacket();
            ReMinecraft.INSTANCE.logger.log("Child user %s connecting!".replace("%s", pck.getUsername()));
            runWhitelist(pck.getUsername());
        }
        if (((MinecraftProtocol) child.getSession().getPacketProtocol()).getSubProtocol() == SubProtocol.GAME) {
            if (event.getRecievedPacket() instanceof ClientKeepAlivePacket) {
                return;
            }
            if (event.getRecievedPacket() instanceof ClientCraftingBookDataPacket) {
                // the recipe book packets seem to cause crash issues on notchian clients right now.
                // todo properly handle and cache recipes?
                return;
            }
            if (event.getRecievedPacket() instanceof ClientPrepareCraftingGridPacket) {
                return;
            }
            if (event.getRecievedPacket() instanceof ClientChatPacket) {
                ClientChatPacket pck = (ClientChatPacket) event.getRecievedPacket();
                if (ReMinecraft.INSTANCE.processInGameCommand(pck.getMessage())) {
                    return;
                }
            }
            if (event.getRecievedPacket() instanceof ClientPlayerPositionPacket) {
                ClientPlayerPositionPacket pck = (ClientPlayerPositionPacket) event.getRecievedPacket();
                ReClient.ReClientCache.posX = pck.getX();
                ReClient.ReClientCache.player.posX = pck.getX();
                ReClient.ReClientCache.posY = pck.getY();
                ReClient.ReClientCache.player.posY = pck.getY();
                ReClient.ReClientCache.posZ = pck.getZ();
                ReClient.ReClientCache.player.posZ = pck.getZ();
                ReClient.ReClientCache.onGround = pck.isOnGround();
            }
            if (event.getRecievedPacket() instanceof ClientPlayerPositionRotationPacket) {
                ClientPlayerPositionRotationPacket pck = (ClientPlayerPositionRotationPacket) event.getRecievedPacket();
                ReClient.ReClientCache.posX = pck.getX();
                ReClient.ReClientCache.player.posX = pck.getX();
                ReClient.ReClientCache.posY = pck.getY();
                ReClient.ReClientCache.player.posY = pck.getY();
                ReClient.ReClientCache.posZ = pck.getZ();
                ReClient.ReClientCache.player.posZ = pck.getZ();
                ReClient.ReClientCache.yaw = (float) pck.getYaw();
                ReClient.ReClientCache.player.yaw = (float) pck.getYaw();
                ReClient.ReClientCache.pitch = (float) pck.getPitch();
                ReClient.ReClientCache.player.pitch = (float) pck.getPitch();
                ReClient.ReClientCache.onGround = pck.isOnGround();
            }
            ReMinecraft.INSTANCE.minecraftClient.getSession().send(event.getRecievedPacket());
        }
    }

    @Override
    public void packetSending(PacketSendingEvent event) {
    }

    /**
     * Invoked when WE send a packet to a CHILD
     */
    @Override
    public void packetSent(PacketSentEvent ev) {
        ChildServerPacketSendEvent event = new ChildServerPacketSendEvent(this.child, ev.getPacket());
        ReMinecraft.INSTANCE.EVENT_BUS.invokeEvent(event);
        if (event.isCancelled()) return;
        if (event.getSendingPacket() instanceof LoginSuccessPacket) {
            LoginSuccessPacket pck = (LoginSuccessPacket) event.getSendingPacket();
            ReMinecraft.INSTANCE.logger.log("Child user " + pck.getProfile().getName() + " authenticated!");
            runWhitelist(pck.getProfile().getName());
        }
        if (event.getSendingPacket() instanceof ServerJoinGamePacket) {
            ReClient.ReClientCache.chunkCache.forEach((hash, chunk) -> {
                this.child.getSession().send(new ServerChunkDataPacket(chunk));
                try {
                    Thread.sleep(5L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            ReMinecraft.INSTANCE.logger.log("Sent " + ReClient.ReClientCache.chunkCache.size() + " chunks");
            this.child.getSession().send(new ServerPluginMessagePacket("MC|Brand", ServerBranding.BRAND_ENCODED));
            this.child.getSession().send(new ServerPlayerChangeHeldItemPacket(ReClient.ReClientCache.heldItem));
            this.child.getSession().send(new ServerPlayerPositionRotationPacket(ReClient.ReClientCache.posX, ReClient.ReClientCache.posY, ReClient.ReClientCache.posZ, ReClient.ReClientCache.yaw, ReClient.ReClientCache.pitch, new Random().nextInt(1000) + 10));
            this.child.getSession().send(new ServerWindowItemsPacket(0, ReClient.ReClientCache.playerInventory));
            ReClient.ReClientCache.playerListEntries.stream()
                    .filter(entry -> entry.getProfile() != null)
                    .forEach(entry -> {
                                try {
                                    Field field = PlayerListEntry.class.getDeclaredField("displayName");
                                    field.setAccessible(true);
                                    field.set(entry, Message.fromString(entry.getProfile().getName()));
                                    if (entry.getProfile().getName() == null) {
                                        Field f = GameProfile.class.getDeclaredField("name");
                                        f.setAccessible(true);
                                        f.set(entry.getProfile(), "???");
                                    }
                                    this.child.getSession().send(new ServerPlayerListEntryPacket(PlayerListEntryAction.ADD_PLAYER, new PlayerListEntry[]{entry}));
                                } catch (IllegalAccessException | NoSuchFieldException e) {
                                    e.printStackTrace();
                                }
                            }
                    );
            //this.child.getSession().send(ReClient.ReClientCache.playerInventory);
            this.child.getSession().send(new ServerPlayerListDataPacket(ReClient.ReClientCache.tabHeader, ReClient.ReClientCache.tabFooter));
            this.child.getSession().send(new ServerPlayerHealthPacket(ReClient.ReClientCache.health, ReClient.ReClientCache.food, ReClient.ReClientCache.saturation));
            for (Entity entity : ReClient.ReClientCache.entityCache.values()) {
                if (entity == null) continue;
                if (entity.type == EntityType.MOB && entity instanceof EntityMob) {
                    EntityMob mob = (EntityMob) entity;
                    this.child.getSession().send(
                            new ServerSpawnMobPacket
                                    (mob.entityId,
                                            mob.uuid,
                                            mob.mobType,
                                            mob.posX,
                                            mob.posY,
                                            mob.posZ,
                                            mob.yaw,
                                            mob.pitch,
                                            mob.headYaw,
                                            mob.motionX,
                                            mob.motionY,
                                            mob.motionZ,
                                            mob.metadata));
                    for (PotionEffect potionEffect : mob.potionEffects) {
                        this.child.getSession().send(new ServerEntityEffectPacket(
                                mob.entityId,
                                potionEffect.effect,
                                potionEffect.amplifier,
                                potionEffect.duration,
                                potionEffect.ambient,
                                potionEffect.showParticles
                        ));
                    }
                    for (Map.Entry<EquipmentSlot, ItemStack> entry : mob.equipment.entrySet()) {
                        this.child.getSession().send(new ServerEntityEquipmentPacket(entity.entityId,
                                entry.getKey(),
                                entry.getValue()));
                    }
                    if (mob.properties.size() > 0) {
                        this.child.getSession().send(new ServerEntityPropertiesPacket(entity.entityId,
                                mob.properties));
                    }
                    continue;
                }
                if (entity.type == EntityType.PLAYER && entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    this.child.getSession().send(new ServerSpawnPlayerPacket(
                            player.entityId,
                            player.uuid,
                            player.posX,
                            player.posY,
                            player.posZ,
                            player.yaw,
                            player.pitch,
                            player.metadata
                    ));
                    for (PotionEffect effect : player.potionEffects) {
                        this.child.getSession().send(new ServerEntityEffectPacket(player.entityId,
                                effect.effect,
                                effect.amplifier,
                                effect.duration,
                                effect.ambient,
                                effect.showParticles));
                    }
                    for (Map.Entry<EquipmentSlot, ItemStack> entry : player.equipment.entrySet()) {
                        this.child.getSession().send(new ServerEntityEquipmentPacket(player.entityId,
                                entry.getKey(),
                                entry.getValue()));
                    }
                    if (player.properties.size() > 0) {
                        this.child.getSession().send(new ServerEntityPropertiesPacket(player.entityId,
                                player.properties));
                    }
                    continue;
                }
                if (entity.type == EntityType.OBJECT && entity instanceof EntityObject) {
                    EntityObject object = (EntityObject) entity;
                    // hello entityobject my old friend ;-;
                    if (object.data == null) {
                        this.child.getSession().send(new ServerSpawnObjectPacket(
                                entity.entityId,
                                entity.uuid,
                                object.objectType,
                                entity.posX,
                                entity.posY,
                                entity.posZ,
                                object.yaw,
                                object.pitch,
                                object.motionX,
                                object.motionY,
                                object.motionZ));
                        continue;
                    }
                    this.child.getSession().send(new ServerSpawnObjectPacket(
                            entity.entityId,
                            entity.uuid,
                            object.objectType,
                            object.data,
                            entity.posX,
                            entity.posY,
                            entity.posZ,
                            object.yaw,
                            object.pitch,
                            object.motionX,
                            object.motionY,
                            object.motionZ));
                    continue;
                }
                ReMinecraft.INSTANCE.logger.logDebug("???");
            }
            for (Entity entity : ReClient.ReClientCache.entityCache.values()) {
                if (entity instanceof EntityEquipment && ((EntityEquipment) entity).passengerIds.length > 0) {
                    this.child.getSession().send(new ServerEntitySetPassengersPacket(entity.entityId,
                            ((EntityEquipment) entity).passengerIds));
                }
                if (entity instanceof EntityRotation) {
                    EntityRotation rotation = (EntityRotation) entity;
                    if (rotation.isLeashed) {
                        this.child.getSession().send(new ServerEntityAttachPacket(entity.entityId,
                                rotation.leashedID));
                    }
                }
            }
            this.child.setPlaying(true);
        }
    }

    @Override
    public void connected(ConnectedEvent event) {
    }

    @Override
    public void disconnecting(DisconnectingEvent event) {
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        event.getCause().printStackTrace();
        ReMinecraft.INSTANCE.logger.log("Child disconnected due to " + event.getReason());
    }

    private void runWhitelist(String name) {
        boolean flag = ReMinecraft.INSTANCE.MAIN_CONFIG.var_useWhitelist && !ReMinecraft.INSTANCE.MAIN_CONFIG.var_whitelistServer
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList())
                .contains(name.toLowerCase());
        if (flag) {
            ReMinecraft.INSTANCE.logger.logWarning(name + " isn't whitelisted.");
            SubProtocol proto = ((MinecraftProtocol) this.child.getSession().getPacketProtocol()).getSubProtocol();
            switch (proto){
                case LOGIN:
                    this.child.getSession().send(new LoginDisconnectPacket(Message.fromString("\247cYou are not whitelisted on this server!\nIf you believe that this is an error, please contact the server administrator")));
                    break;
                case GAME:
                    this.child.getSession().send(new ServerDisconnectPacket(Message.fromString("\247cYou are not whitelisted on this server!\nIf you believe that this is an error, please contact the server administrator")));
                    break;
            }
            this.child.getSession().disconnect("Not whitelisted!");
            return;
        }
    }
}

class ServerBranding {
    public static final String BRAND = "RE:Minecraft " + ReMinecraft.VERSION;
    public static byte[] BRAND_ENCODED;

    static {
        ByteBuf buf = Unpooled.buffer(5 + BRAND.length());
        try {
            writeUTF8(buf, BRAND);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BRAND_ENCODED = buf.array();
    }

    private static void writeUTF8(ByteBuf buf, String value) throws IOException {
        final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length >= Short.MAX_VALUE) {
            throw new IOException("Attempt to write a string with a length greater than Short.MAX_VALUE to ByteBuf!");
        }
        // Write the string's length
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }

    private static void writeVarInt(ByteBuf buf, int value) {
        byte part;
        while (true) {
            part = (byte) (value & 0x7F);
            value >>>= 7;
            if (value != 0) {
                part |= 0x80;
            }
            buf.writeByte(part);
            if (value == 0) {
                break;
            }
        }
    }


}
