package com.sasha.reminecraft.reaction.server;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.reaction.IPacketReactor;

public class ClientChatReaction implements IPacketReactor<ClientChatPacket> {

    @Override
    public boolean takeAction(ClientChatPacket packet) {
        return !ReMinecraft.INSTANCE.processInGameCommand(packet.getMessage());
    }
}
