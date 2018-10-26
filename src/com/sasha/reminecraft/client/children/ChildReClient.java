package com.sasha.reminecraft.client.children;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.packetlib.Session;

/**
 * The child client that is trying to play on the logged in Mojang account
 */
public class ChildReClient {

    private Session session;
    private boolean playing;

    public ChildReClient(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }


    public boolean isPlaying() {
        var pckprot = (MinecraftProtocol) session.getPacketProtocol();
        return playing && pckprot.getSubProtocol() == SubProtocol.GAME;
    }
}
