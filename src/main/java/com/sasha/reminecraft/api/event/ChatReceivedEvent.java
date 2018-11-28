package com.sasha.reminecraft.api.event;

import com.sasha.eventsys.SimpleEvent;

/**
 * Invoked when a message is recieved from the remote server
 */
public class ChatReceivedEvent extends SimpleEvent {

    public String messageText;
    public long timeRecieved;
    public String messageAuthor;

    public ChatReceivedEvent(String messageText, long timeRecieved) {
        this.messageText = messageText;
        this.timeRecieved = timeRecieved;
        if (messageText.startsWith("<")) messageAuthor = messageText.replaceAll(".*<.*?>.*", "");
        else messageAuthor = null;
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

    public String getMessageAuthor() {
        return messageAuthor;
    }

    public boolean hasMessageAuthor() {
        return messageAuthor != null;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
