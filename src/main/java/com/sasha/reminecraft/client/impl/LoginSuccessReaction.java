package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.login.server.LoginSuccessPacket;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;

public class LoginSuccessReaction implements IPacketReactor<LoginSuccessPacket> {
    @Override
    public boolean takeAction(LoginSuccessPacket pck) {
        ReClient.ReClientCache.INSTANCE.uuid = pck.getProfile().getId();
        return true;
    }
}
