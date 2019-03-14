package com.sasha.reminecraft.api.event;

import com.sasha.eventsys.SimpleCancellableEvent;
import com.sasha.eventsys.SimpleEvent;
import com.sasha.reminecraft.util.PingStatus;

public class ServerPingEvent {

    public static class Pre extends SimpleCancellableEvent {

    }

    public static class Post extends SimpleEvent {
        private long milliseconds;
        private PingStatus status;

        public Post(long ms, PingStatus status) {
            this.milliseconds = ms;
            this.status = status;
        }

        public long getPing() {
            return milliseconds;
        }

        public void setStatus(PingStatus status) {
            this.status = status;
        }

        public PingStatus getStatus() {
            return status;
        }
    }

}
