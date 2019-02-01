package com.sasha.reminecraft.util;

import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.data.status.handler.ServerPingTimeHandler;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.logging.ILogger;

import java.net.Proxy;

public class ServerPinger {

    private String host;
    private int port;
    public long ms = -1L;
    public PingStatus pinged = PingStatus.PINGING;

    public ServerPinger(String host) {
        this(host, 25565);
    }

    public ServerPinger(String host, int port) {
        this.host = host;
        this.port = port;
    }


    public void status(ILogger logger) {
        try {
            MinecraftProtocol protocol = new MinecraftProtocol(SubProtocol.STATUS);
            Client client = new Client(host, port, protocol, new TcpSessionFactory(Proxy.NO_PROXY));
            client.getSession().setFlag(MinecraftConstants.AUTH_PROXY_KEY, Proxy.NO_PROXY);
            client.getSession().setFlag(MinecraftConstants.SERVER_PING_TIME_HANDLER_KEY, (ServerPingTimeHandler) (session, pingTime) -> {
                logger.log("Server is alive, will connect. (pong received after " + pingTime + " ms)");
                ms = pingTime;
                pinged = PingStatus.PINGED;
                ReMinecraft.INSTANCE.notify();
            });
            client.getSession().connect(true);
        } catch (Exception e) {
            pinged = PingStatus.DEAD;
            ReMinecraft.INSTANCE.notify();
        }

    }

}
