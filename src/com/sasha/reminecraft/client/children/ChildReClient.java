package com.sasha.reminecraft.client.children;

import com.github.steveice10.packetlib.Session;

/**
 * The child client that is trying to play on the logged in Mojang account
 */

public class ChildReClient {

    private Session session;

    public ChildReClient(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }
}
