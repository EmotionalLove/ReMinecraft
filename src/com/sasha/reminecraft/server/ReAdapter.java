package com.sasha.reminecraft.server;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntryAction;
import com.github.steveice10.mc.protocol.data.game.entity.EquipmentSlot;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.ItemStack;
import com.github.steveice10.mc.protocol.data.game.setting.Difficulty;
import com.github.steveice10.mc.protocol.data.game.world.WorldType;
import com.github.steveice10.mc.protocol.data.message.TextMessage;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientKeepAlivePacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPlayerListDataPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPlayerListEntryPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPluginMessagePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerChangeHeldItemPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerHealthPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnMobPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnObjectPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import com.github.steveice10.mc.protocol.packet.login.client.LoginStartPacket;
import com.github.steveice10.mc.protocol.packet.login.server.LoginSuccessPacket;
import com.github.steveice10.packetlib.event.session.*;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.client.ReListener;
import com.sasha.reminecraft.client.children.ChildReClient;
import com.sasha.reminecraft.util.entity.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;

public class ReAdapter extends SessionAdapter {

    private ChildReClient child;

    public ReAdapter(ChildReClient child) {
        this.child = child;
    }

    /**
     * Invoked when the child sends us a packet
     */
    @Override
    public void packetReceived(PacketReceivedEvent event) {
        var protocol = (MinecraftProtocol) child.getSession().getPacketProtocol();
        if (event.getPacket() instanceof LoginStartPacket && (protocol.getSubProtocol() == SubProtocol.LOGIN || protocol.getSubProtocol() == SubProtocol.HANDSHAKE)) {
            var pck = (LoginStartPacket) event.getPacket();
            ReMinecraft.INSTANCE.logger.log("Child user %s connecting!".replace("%s", pck.getUsername()));
        }
        if (((MinecraftProtocol) child.getSession().getPacketProtocol()).getSubProtocol() == SubProtocol.GAME) {
            if (event.getPacket() instanceof ClientKeepAlivePacket) {
                return;
            }
            if (event.getPacket() instanceof ClientPlayerPositionPacket) {
                var pck = (ClientPlayerPositionPacket) event.getPacket();
                ReListener.ReListenerCache.posX = pck.getX();
                ReListener.ReListenerCache.player.posX = pck.getX();
                ReListener.ReListenerCache.posY = pck.getY();
                ReListener.ReListenerCache.player.posY = pck.getY();
                ReListener.ReListenerCache.posZ = pck.getZ();
                ReListener.ReListenerCache.player.posZ = pck.getZ();
                ReListener.ReListenerCache.onGround = pck.isOnGround();
            }
            if (event.getPacket() instanceof ClientPlayerPositionRotationPacket) {
                var pck = (ClientPlayerPositionRotationPacket) event.getPacket();
                ReListener.ReListenerCache.posX = pck.getX();
                ReListener.ReListenerCache.player.posX = pck.getX();
                ReListener.ReListenerCache.posY = pck.getY();
                ReListener.ReListenerCache.player.posY = pck.getY();
                ReListener.ReListenerCache.posZ = pck.getZ();
                ReListener.ReListenerCache.player.posZ = pck.getZ();
                ReListener.ReListenerCache.yaw = (float) pck.getYaw();
                ReListener.ReListenerCache.player.yaw = (float) pck.getYaw();
                ReListener.ReListenerCache.pitch = (float) pck.getPitch();
                ReListener.ReListenerCache.player.pitch = (float) pck.getPitch();
                ReListener.ReListenerCache.onGround = pck.isOnGround();
            }
            ReMinecraft.INSTANCE.minecraftClient.getSession().send(event.getPacket());
        }
    }

    @Override
    public void packetSending(PacketSendingEvent event) {
    }

    /**
     * Invoked when WE send a packet to a CHILD
     */
    @Override
    public void packetSent(PacketSentEvent event) {
        if (event.getPacket() instanceof LoginSuccessPacket) {
            var pck = (LoginSuccessPacket) event.getPacket();
            ReMinecraft.INSTANCE.logger.log("Child user " + pck.getProfile().getName() + " authenticated!");
            /*this.child.getSession().send(new ServerJoinGamePacket(
                    ReListener.ReListenerCache.entityId,
                    false,
                    ReListener.ReListenerCache.gameMode,
                    ReListener.ReListenerCache.dimension,
                    Difficulty.NORMAL,
                    1,
                    WorldType.DEFAULT,
                    true));*/
        }
        if (event.getPacket() instanceof ServerJoinGamePacket) {
            ReListener.ReListenerCache.chunkCache.forEach((hash, chunk) -> {
                this.child.getSession().send(new ServerChunkDataPacket(chunk));
                try {
                    Thread.sleep(5L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            ReMinecraft.INSTANCE.logger.log("Sent " + ReListener.ReListenerCache.chunkCache.size() + " chunks");
            this.child.getSession().send(new ServerPluginMessagePacket("MC|Brand", ServerBranding.BRAND_ENCODED));
            this.child.getSession().send(new ServerPlayerChangeHeldItemPacket(ReListener.ReListenerCache.heldItem));
            this.child.getSession().send(new ServerPlayerPositionRotationPacket(ReListener.ReListenerCache.posX, ReListener.ReListenerCache.posY, ReListener.ReListenerCache.posZ, ReListener.ReListenerCache.yaw, ReListener.ReListenerCache.pitch, new Random().nextInt(1000) + 10));
            ReListener.ReListenerCache.playerListEntries.stream()
                    .filter(entry -> entry.getProfile() != null)
                    .forEach(entry -> {
                                try {
                                    var field = PlayerListEntry.class.getDeclaredField("displayName");
                                    field.setAccessible(true);
                                    field.set(entry, new TextMessage(entry.getProfile().getName()));
                                    this.child.getSession().send(new ServerPlayerListEntryPacket(PlayerListEntryAction.ADD_PLAYER, new PlayerListEntry[]{entry}));
                                } catch (IllegalAccessException | NoSuchFieldException e) {
                                    e.printStackTrace();
                                }
                            }
                    );
            //this.child.getSession().send(ReListener.ReListenerCache.playerInventory);
            this.child.getSession().send(new ServerPlayerListDataPacket(ReListener.ReListenerCache.tabHeader, ReListener.ReListenerCache.tabFooter));
            this.child.getSession().send(new ServerPlayerHealthPacket(ReListener.ReListenerCache.health, ReListener.ReListenerCache.food, ReListener.ReListenerCache.saturation));
            for (Entity entity : ReListener.ReListenerCache.entityCache.values()) {
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
                ReMinecraft.INSTANCE.logger.log("??? There's a problem, Sasha!");
            }
            for (Entity entity : ReListener.ReListenerCache.entityCache.values()) {
                if (entity instanceof EntityEquipment) {
                    if (((EntityEquipment) entity).passengerIds.length > 0) {
                        this.child.getSession().send(new ServerEntitySetPassengersPacket(entity.entityId,
                                ((EntityEquipment) entity).passengerIds));
                    }
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
