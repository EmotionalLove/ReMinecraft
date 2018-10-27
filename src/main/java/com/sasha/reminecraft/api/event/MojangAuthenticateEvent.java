package com.sasha.reminecraft.api.event;

import com.sasha.eventsys.SimpleCancellableEvent;
import com.sasha.eventsys.SimpleEvent;

public class MojangAuthenticateEvent {

    public static class Pre extends SimpleCancellableEvent {
        private Method method;

        public Pre(Method method) {
            this.method = method;
        }

        public Method getMethod() {
            return method;
        }
    }

    public static class Post extends SimpleEvent {
        private boolean success;

        public Post(boolean success) {
            this.success = success;
        }

        public boolean isSuccessful() {
            return success;
        }
    }

    public enum Method {
        EMAILPASS, SESSIONID
    }

}
