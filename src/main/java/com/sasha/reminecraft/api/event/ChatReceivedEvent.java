package com.sasha.reminecraft.api.event;

import com.sasha.eventsys.SimpleEvent;
import com.sun.istack.internal.NotNull;

/**
 * Invoked when a message is recieved from the remote server
 */
public class ChatReceivedEvent extends SimpleEvent {

    private String messageRaw;
    private String messageText;
    private long timeRecieved;
    private String messageAuthor;

    public ChatReceivedEvent(String messageText, long timeRecieved) {
        this.messageRaw = messageText;
        this.timeRecieved = timeRecieved;
        if (messageText.startsWith("<")) {
            this.messageText = messageText.replaceFirst(".*<.*?>.", "");
            this.messageAuthor = messageText.substring(0, messageText.indexOf(">") - 1);
            //this.messageAuthor = messageText.replaceAll(".*<.*?>.*", "");
        } else {
            this.messageAuthor = null;
            this.messageText = messageText;
        }
    }

    public ChatReceivedEvent(String messageText, @NotNull String messageAuthor, long timeRecieved) {
        this.messageText = messageText;
        this.timeRecieved = timeRecieved;
        this.messageAuthor = messageAuthor;
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

    public String getMessageRaw() {
        return messageRaw;
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
