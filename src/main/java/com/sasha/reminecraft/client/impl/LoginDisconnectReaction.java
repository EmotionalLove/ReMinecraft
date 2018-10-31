package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.login.server.LoginDisconnectPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.client.IPacketReactor;

public class LoginDisconnectReaction implements IPacketReactor<LoginDisconnectPacket> {
    @Override
    public boolean takeAction(LoginDisconnectPacket packet) {
        ReMinecraft.INSTANCE.logger.logError("Kicked whilst logging in: " + packet.getReason().getFullText());
        ReMinecraft.INSTANCE.minecraftClient.getSession().disconnect(packet.getReason().getFullText(), true);
        return true;
    }
}
