package com.sasha.reminecraft.server;

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
