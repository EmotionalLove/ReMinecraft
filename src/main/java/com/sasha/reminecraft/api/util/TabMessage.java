package com.sasha.reminecraft.api.util;

import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.data.message.Message;
import com.sasha.reminecraft.client.ReClient;

import java.util.List;

public class TabMessage {


    public String separator = ", ";

    public Message tabHeader, tabFooter;
    public List<PlayerListEntry> playerList;

    public long time;

    public TabMessage(){
        this.tabHeader = ReClient.ReClientCache.INSTANCE.tabHeader;
        this.tabFooter = ReClient.ReClientCache.INSTANCE.tabFooter;
        this.playerList = ReClient.ReClientCache.INSTANCE.playerListEntries;
        this.time = System.currentTimeMillis();
    }


    public String getQueuePos() {
        String rV;
        try{
            rV = this.tabHeader.getFullText().split("\n")[5].split(": ")[1].substring(2);
        } catch (Exception e){ rV = null; }
        return rV;
    }

    public String getQueueEstimatedTime(){
        String rV;
        try{
            rV = this.tabHeader.getFullText().split("\n")[6].split(": ")[1].substring(2);
        } catch(Exception e){
            rV = null;
        }
        return rV;
    }

    public String[] getTabHeader(){
        String[] rV = this.tabHeader.getFullText().split("\n");
        return rV;
    }

    // TODO an equals




    public String toQueueString(){
        return this.time + this.separator + this.getQueuePos() + this.separator + this.getQueueEstimatedTime();
    }
}
