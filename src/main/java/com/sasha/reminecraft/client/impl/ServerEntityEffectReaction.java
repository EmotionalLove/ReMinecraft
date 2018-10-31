package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityEffectPacket;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.entity.EntityEquipment;
import com.sasha.reminecraft.util.entity.PotionEffect;

public class ServerEntityEffectReaction implements IPacketReactor<ServerEntityEffectPacket> {
    @Override
    public boolean takeAction(ServerEntityEffectPacket pck) {
        PotionEffect effect = new PotionEffect();
        effect.effect = pck.getEffect();
        effect.amplifier = pck.getAmplifier();
        effect.duration = pck.getDuration();
        effect.ambient = pck.isAmbient();
        effect.showParticles = pck.getShowParticles();
        ((EntityEquipment) ReClient.ReClientCache.INSTANCE.entityCache.get(pck.getEntityId())).potionEffects.add(effect);
        return true;
    }
}
