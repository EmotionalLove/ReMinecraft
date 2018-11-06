package com.sasha.reminecraft.reaction;

import com.sasha.reminecraft.client.ChildReClient;

/**
 * Used when we need to access the relevant child during a reaction.
 */
public abstract class AbstractChildPacketReactor {

    private ChildReClient child;

    public void setChild(ChildReClient child) {
        this.child = child;
    }

    public ChildReClient getChild() {
        return child;
    }
}
