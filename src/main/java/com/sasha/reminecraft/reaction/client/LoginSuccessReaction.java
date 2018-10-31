package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.login.server.LoginSuccessPacket;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;

public class LoginSuccessReaction implements IPacketReactor<LoginSuccessPacket> {
    @Override
    public boolean takeAction(LoginSuccessPacket packet) {
        ReClient.ReClientCache.INSTANCE.uuid = packet.getProfile().getId();
        return true;
    }
}
