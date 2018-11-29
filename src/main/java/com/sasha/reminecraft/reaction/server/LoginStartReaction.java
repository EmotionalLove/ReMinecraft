package com.sasha.reminecraft.reaction.server;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.packet.login.client.LoginStartPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.reaction.AbstractChildPacketReactor;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.server.ReServer;

public class LoginStartReaction extends AbstractChildPacketReactor implements IPacketReactor<LoginStartPacket> {


    @Override
    public boolean takeAction(LoginStartPacket packet) {
        if (((MinecraftProtocol)this.getChild().getSession().getPacketProtocol()).getSubProtocol() != SubProtocol.LOGIN) {
            return false;
        }
        ReMinecraft.LOGGER.log("Child user %s connecting!".replace("%s", packet.getUsername()));
        ReServer.runWhitelist(packet.getUsername(), this.getChild());
        return false;
    }
}
