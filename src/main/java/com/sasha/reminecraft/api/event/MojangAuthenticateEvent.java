package com.sasha.reminecraft.api.event;

import com.sasha.eventsys.SimpleCancellableEvent;

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

    public static class Post extends SimpleCancellableEvent {
        private boolean success;
        private Method method;

        public Post(Method method, boolean success) {
            this.success = success;
            this.method = method;
        }

        public Method getMethod() {
            return method;
        }

        public boolean isSuccessful() {
            return success;
        }
    }

    public enum Method {
        EMAILPASS, SESSIONID
    }

}
