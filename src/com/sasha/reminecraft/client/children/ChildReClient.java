package com.sasha.reminecraft.client.children;

import com.github.steveice10.packetlib.Session;
import com.sasha.reminecraft.server.ReAdapter;

/**
 * The child client that is trying to play on the logged in Mojang account
 */

public class ChildReClient {

    private Session session;
    private boolean playing;

    public ChildReClient(Session session) {
        this.session = session;
        this.session.addListener(new ReAdapter(this));
    }

    public Session getSession() {
        return session;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isPlaying() {
        return playing;
    }
}
