package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerUpdateTimePacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.reaction.IPacketReactor;
import com.sasha.reminecraft.server.ReServerManager;

public class ServerUpdateTimeReaction implements IPacketReactor<ServerUpdateTimePacket> {
    @Override
    public boolean takeAction(ServerUpdateTimePacket packet) {
        if (!ReClient.ReClientCache.INSTANCE.serverTicked) {
            ReClient.ReClientCache.INSTANCE.serverTicked = true;
            ReMinecraft.LOGGER.log("Starting server on " + ReMinecraft.INSTANCE.MAIN_CONFIG.var_hostServerIp + ":" +
                    ReMinecraft.INSTANCE.MAIN_CONFIG.var_hostServerPort);
            try {
                ReMinecraft.INSTANCE.minecraftServer = ReServerManager.prepareServer();
                ReMinecraft.INSTANCE.minecraftServer.addListener(new ReServerManager());
                ReMinecraft.INSTANCE.minecraftServer.bind(true);
            } catch (Exception e) {
                e.printStackTrace();
                ReMinecraft.LOGGER.logError("A severe exception occurred whilst creating the server! Maybe there's already a server running on the port?");
            }
            ReMinecraft.LOGGER.log("Server started!");
        }
        return true;
    }
}
