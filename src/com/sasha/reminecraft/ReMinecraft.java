package com.sasha.reminecraft;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The main Re:Minecraft class, where the majority of essential startup functions will be stored.
 */
public class ReMinecraft {

    /**
     * Singleton of this Re:Minecraft
     */
    public static ReMinecraft INSTANCE;

    /**
     * Current software version of Re:Minecraft
     */
    public static final String VERSION = "1.0a";

    public Client minecraftClient = null;
    public Server minecraftServer = null;
    public MinecraftProtocol protocol;
    public List<ChildReClient> childClients = new ArrayList<>();
    public HashMap<Session, ChildReClient> sessionClients = new LinkedHashMap<>();
    /**
     * Launch Re:Minecraft and and setup the console command system.
     */
    public static void main(String[] args) {
        // TODO
    }

    /**
     * Launch (or relaunch) Re:Minecraft
     */
    public void start() {
        // TODO
    }

}
