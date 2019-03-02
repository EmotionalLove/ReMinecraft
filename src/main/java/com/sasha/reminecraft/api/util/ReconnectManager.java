package com.sasha.reminecraft.api.util;

import com.sasha.reminecraft.ReMinecraft;

public class ReconnectManager {


    public static void reconnect(){
        restart();
    }

    public static void reconnect(int time){
        // TODO make a disconnect and connect method (so the bot is not connected to a server but the children still are connected)
        restart(time);
    }

    public static void restart(int time){
        ReMinecraft.INSTANCE.reLaunch(time);
    }

    public static void restart(){
        ReMinecraft.INSTANCE.reLaunch();
    }

    public static void shutDown(){
        ReMinecraft.INSTANCE.stop();
    }

}
