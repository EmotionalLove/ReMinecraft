package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityEffectPacket;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.util.entity.EntityEquipment;
import com.sasha.reminecraft.util.entity.PotionEffect;

public class ServerEntityEffectReaction implements IPacketReactor<ServerEntityEffectPacket> {
    @Override
    public boolean takeAction(ServerEntityEffectPacket packet) {
        PotionEffect effect = new PotionEffect();
        effect.effect = packet.getEffect();
        effect.amplifier = packet.getAmplifier();
        effect.duration = packet.getDuration();
        effect.ambient = packet.isAmbient();
        effect.showParticles = packet.getShowParticles();
        ((EntityEquipment) ReClient.ReClientCache.INSTANCE.entityCache.get(packet.getEntityId())).potionEffects.add(effect);
        return true;
    }
}
