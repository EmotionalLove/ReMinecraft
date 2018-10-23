package com.sasha.reminecraft.client;

import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.packetlib.event.session.*;
import com.sasha.reminecraft.ReMinecraft;

/**
 * Listens and processes packets being Tx'd and Rx'd from the remote server.
 */
public class ReListener implements SessionListener {

    /**
     * Invoked when a packet is recieved
     */
    @Override
    public void packetReceived(PacketReceivedEvent packetReceivedEvent) {
        if (packetReceivedEvent.getPacket() instanceof ServerChatPacket) {
            ReMinecraft.INSTANCE.logger.log(((ServerChatPacket) packetReceivedEvent.getPacket()).getMessage().getFullText());
        }
    }

    /**
     * Invoked when a packet prepares to be sent
     */
    @Override
    public void packetSending(PacketSendingEvent packetSendingEvent) {

    }

    /**
     * Invoked when a packet has been successfully dispatched
     */
    @Override
    public void packetSent(PacketSentEvent packetSentEvent) {

    }

    /**
     * Invoked when the client connects to the remote server
     */
    @Override
    public void connected(ConnectedEvent connectedEvent) {
        ReMinecraft.INSTANCE.logger.log
                ("Connected to " + connectedEvent.getSession().getHost() +
                        ":"
                        + connectedEvent.getSession().getPort());
    }

    /**
     * Invoked when the client is disconnecting from the remote server
     */
    @Override
    public void disconnecting(DisconnectingEvent disconnectingEvent) {

    }

    /**
     * Invoked when the client has been disconnected from the remote server
     * =
     */
    @Override
    public void disconnected(DisconnectedEvent disconnectedEvent) {
        ReMinecraft.INSTANCE.logger.logWarning("Disconnected: " + disconnectedEvent.getReason());
    }
}
