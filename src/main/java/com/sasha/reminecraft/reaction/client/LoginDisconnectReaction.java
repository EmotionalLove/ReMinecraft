package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.login.server.LoginDisconnectPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.reaction.IPacketReactor;

public class LoginDisconnectReaction implements IPacketReactor<LoginDisconnectPacket> {
    @Override
    public boolean takeAction(LoginDisconnectPacket packet) {
        ReMinecraft.INSTANCE.terminalLogger.logError("Kicked whilst logging in: " + packet.getReason().getFullText());
        ReMinecraft.INSTANCE.minecraftClient.getSession().disconnect(packet.getReason().getFullText(), true);
        return true;
    }
}
