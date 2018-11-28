package com.sasha.reminecraft.api.event;

import com.sasha.eventsys.SimpleEvent;

/**
 * Invoked when a message is recieved from the remote server
 */
public class ChatReceivedEvent extends SimpleEvent {

    public String messageText;
    public long timeRecieved;
    public String messageAuthor;
    
    public ChatReceivedEvent(String messageText,String messageAuthor, long timeRecieved) {
        this.messageText = messageText;
        this.timeRecieved = timeRecieved;
        if(messageAuthor.startsWith("<"){
            messageAuthor = event.messageText.replaceAll(".*\\<|\\>.*", ""); 
        }else{
            messageAuthor =null;
        }
        this.messageAuthor=messageAuthor
    }     
    public String getMessageText() {
        return messageText;
    }
        this.messageAuthor = messageAuthor
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
