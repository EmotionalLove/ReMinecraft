package com.sasha.reminecraft.api.util;

import com.sasha.reminecraft.api.event.ChatReceivedEvent;

public class ChatMessage {


    public String separator;
    public boolean isLink;

    public long time;
    public String msg;

    private ChatMessage(){
        this.separator = ": ";
        this.isLink = false;
    }

    public ChatMessage(ChatReceivedEvent e){
        this();

        this.time = e.getTimeRecieved();
        this.msg = e.getMessageText();
    }



    /**
     * for spam msg pointing
     * @param nevMsg
     */
    public ChatMessage setLink(String nevMsg){
        this.isLink = true;
        this.msg = nevMsg;
        return this;
    }

    public boolean isConnecting(){
        return ("Connecting to the server...").equals(msg);
    }

    public boolean inQueue() {

        if (msg.startsWith("<"))
            return false;

        if (("2b2t is full").equals(msg))
            return true;

        if(isConnecting())
            return true;

        if (msg.startsWith("Position in queue: "))
            return true;

        if(msg.startsWith("Exception Connecting:")) // to catch other connection errors if they ever come up
            return true;

        if(("Lost connection to server").equals(msg))
            return true;

        // act who knows wether in queue or not?
        return false;
    }

    @Override
    public String toString() {
        if(isLink)
            return "P(" + time + ")" + separator + msg;
        return time + separator + msg;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ChatMessage)
            return equals((ChatMessage) obj);
        return super.equals(obj);
    }

    public boolean equals(ChatMessage cm) {
        if(cm == null)
            return false;
        return this.msg.equals(cm.msg);
    }
}
