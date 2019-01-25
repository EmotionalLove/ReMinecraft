package com.sasha.reminecraft.server;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.ServerLoginHandler;
import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.data.game.setting.Difficulty;
import com.github.steveice10.mc.protocol.data.game.world.WorldType;
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
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.client.ChildReClient;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.util.TextMessageColoured;

import java.net.Proxy;

public class ReServerManager extends ServerAdapter {

    public void sessionAdded(SessionAddedEvent event) {
        ChildReClient cli = new ChildReClient(event.getSession());
        ReServer adapter = new ReServer(cli);
        ReMinecraft.INSTANCE.childClients.add(cli);
        ReMinecraft.INSTANCE.childAdapters.put(cli, adapter);
        event.getSession().addListener(adapter);
    }

    public void sessionRemoved(SessionRemovedEvent event) {
        getClientBySession(event.getSession()).setPlaying(false);
        ReMinecraft.INSTANCE.childClients.remove(getClientBySession(event.getSession()));
        event.getSession().removeListener(ReMinecraft.INSTANCE.childAdapters.get(getClientBySession(event.getSession())));
    }

    public static ChildReClient getClientBySession(Session session) {
        for (ChildReClient childClient : ReMinecraft.INSTANCE.childClients) {
            if (childClient.getSession().getHost().equals(session.getHost())
                    && childClient.getSession().getPort() == session.getPort()) {
                return childClient;
            }
        }
        return null;
    }

    /**
     * Build an instance of the server
     *
     * @return the built server
     */
    public static Server prepareServer() {
        Server server = new Server(ReMinecraft.INSTANCE.MAIN_CONFIG.var_hostServerIp, ReMinecraft.INSTANCE.MAIN_CONFIG.var_hostServerPort, MinecraftProtocol.class, new TcpSessionFactory());
        server.setGlobalFlag(MinecraftConstants.AUTH_PROXY_KEY, Proxy.NO_PROXY);
        server.setGlobalFlag(MinecraftConstants.VERIFY_USERS_KEY, ReMinecraft.INSTANCE.MAIN_CONFIG.var_onlineModeServer);
        server.setGlobalFlag
                (MinecraftConstants.SERVER_INFO_BUILDER_KEY, new ServerInfoBuilder() {
                    @Override
                    public ServerStatusInfo buildInfo(Session session) {
                        return new ServerStatusInfo(
                                new VersionInfo(MinecraftConstants.GAME_VERSION, MinecraftConstants.PROTOCOL_VERSION),
                                new PlayerInfo(420, (int) ReMinecraft.INSTANCE.childClients.stream().filter(e -> ((MinecraftProtocol) e.getSession().getPacketProtocol()).getSubProtocol() != SubProtocol.STATUS).count(), new GameProfile[]{}),
                                TextMessageColoured.from(ReMinecraft.INSTANCE.MAIN_CONFIG.var_messageOfTheDay),
                                null);
                    }
                });
        server.setGlobalFlag(MinecraftConstants.SERVER_COMPRESSION_THRESHOLD, 256);
        server.setGlobalFlag(MinecraftConstants.SERVER_LOGIN_HANDLER_KEY, (ServerLoginHandler) session -> session.send(new ServerJoinGamePacket(
                ReClient.ReClientCache.INSTANCE.entityId,
                false,
                ReClient.ReClientCache.INSTANCE.gameMode,
                ReClient.ReClientCache.INSTANCE.dimension,
                Difficulty.NORMAL,
                1,
                WorldType.DEFAULT,
                false)));
        return server;
    }

}
