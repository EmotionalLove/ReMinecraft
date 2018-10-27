package com.sasha.reminecraft.api.event;

import com.sasha.eventsys.SimpleEvent;

/**
 * Invoked when a message is recieved from the remote server
 */
public class ChatRecievedEvent extends SimpleEvent {

    public String messageText;
    public long timeRecieved;

    public ChatRecievedEvent(String messageText, long timeRecieved) {
        this.messageText = messageText;
        this.timeRecieved = timeRecieved;
    }

    public String getMessageText() {
        return messageText;
    }

    /**
     * In milliseconds
     */
    public long getTimeRecieved() {
        return timeRecieved;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
