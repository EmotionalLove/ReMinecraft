package com.sasha.reminecraft.server;

import com.github.steveice10.mc.protocol.packet.login.server.LoginSuccessPacket;
import com.github.steveice10.packetlib.event.session.*;
import com.sasha.reminecraft.ReMinecraft;

public class ReAdapter extends SessionAdapter {

    @Override
    public void packetReceived(PacketReceivedEvent event) {
    }

    @Override
    public void packetSending(PacketSendingEvent event) {
    }

    @Override
    public void packetSent(PacketSentEvent event) {
        if (event.getPacket() instanceof LoginSuccessPacket) {
            var pck = (LoginSuccessPacket) event.getPacket();
            ReMinecraft.INSTANCE.logger.log("Child user %s connecting!".replace("%s", pck.getProfile().getName()));
        }
    }

    @Override
    public void connected(ConnectedEvent event) {
        ReMinecraft.INSTANCE.logger.log("someone has connected");
    }

    @Override
    public void disconnecting(DisconnectingEvent event) {
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
    }

}
