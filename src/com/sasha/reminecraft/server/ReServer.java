package com.sasha.reminecraft.server;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.ServerLoginHandler;
import com.github.steveice10.mc.protocol.data.game.setting.Difficulty;
import com.github.steveice10.mc.protocol.data.game.world.WorldType;
import com.github.steveice10.mc.protocol.data.message.TextMessage;
import com.github.steveice10.mc.protocol.data.status.PlayerInfo;
import com.github.steveice10.mc.protocol.data.status.ServerStatusInfo;
import com.github.steveice10.mc.protocol.data.status.VersionInfo;
import com.github.steveice10.mc.protocol.data.status.handler.ServerInfoBuilder;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.server.ServerAdapter;
import com.github.steveice10.packetlib.event.server.SessionAddedEvent;
import com.github.steveice10.packetlib.event.server.SessionRemovedEvent;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;
import com.sasha.reminecraft.Configuration;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.client.ReListener;
import com.sasha.reminecraft.client.children.ChildReClient;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.net.Proxy;
import java.rmi.Remote;

public class ReServer extends ServerAdapter {

    public void sessionAdded(SessionAddedEvent event) {
        ReMinecraft.INSTANCE.childClients.add(new ChildReClient(event.getSession()));
    }

    public void sessionRemoved(SessionRemovedEvent event) {
        ReMinecraft.INSTANCE.childClients.remove(getClientBySession(event.getSession()));
    }

    @Nullable
    public static ChildReClient getClientBySession(Session session) {
        for (ChildReClient childClient : ReMinecraft.INSTANCE.childClients) {
            if (childClient.getSession() == session) {
                return childClient;
            }
        }
        return null;
    }

    public static Server prepareServer() {
        Server server = new Server(Configuration.var_hostServerIp, Configuration.var_hostServerPort, MinecraftProtocol.class, new TcpSessionFactory());
        server.setGlobalFlag(MinecraftConstants.AUTH_PROXY_KEY, Proxy.NO_PROXY);
        server.setGlobalFlag(MinecraftConstants.VERIFY_USERS_KEY, Configuration.var_onlineModeServer);
        server.setGlobalFlag
                (MinecraftConstants.SERVER_INFO_BUILDER_KEY,
                        new ServerInfoBuilder() {
                            @Override
                            public ServerStatusInfo buildInfo(Session session) {
                                return new ServerStatusInfo(
                                        new VersionInfo(MinecraftConstants.GAME_VERSION, MinecraftConstants.PROTOCOL_VERSION),
                                        new PlayerInfo(1, 0, new GameProfile[]{}),
                                        new TextMessage(Configuration.var_messageOfTheDay),
                                        new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB));
                            }
                        });
        server.setGlobalFlag(MinecraftConstants.SERVER_COMPRESSION_THRESHOLD, 256);
        return server;
    }

}
