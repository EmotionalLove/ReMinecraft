package com.sasha.reminecraft;

import com.github.steveice10.mc.auth.service.AuthenticationService;
import com.github.steveice10.packetlib.packet.Packet;

import java.net.Proxy;

public interface IReMinecraft {

    void sendToChildren(Packet pck);
    void start(String[] args);
    AuthenticationService authenticate(Proxy proxy);
    void updateToken(String token);
    boolean areChildrenConnected();
    void registerCommands();
    boolean processInGameCommand(String s);
    void registerConfigurations();
    void stop();
    void stopSoft();
    void reLaunch();
}
