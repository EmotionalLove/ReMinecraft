package com.sasha.reminecraft;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.Session;

import java.util.*;

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

    public Logger logger = new Logger("RE:Minecraft " + VERSION);
    public Client minecraftClient = null;
    public Server minecraftServer = null;
    public MinecraftProtocol protocol;
    public List<ChildReClient> childClients = new ArrayList<>();
    public HashMap<Session, ChildReClient> sessionClients = new LinkedHashMap<>();

    /**
     * Launch Re:Minecraft and and setup the console command system.
     */
    public static void main(String[] args) {
        new ReMinecraft().start(args); // start Re:Minecraft before handling console commands
        Scanner scanner = new Scanner(System.in);
        String cmd = scanner.nextLine();
        // TODO commands
    }

    /**
     * Launch (or relaunch) Re:Minecraft
     */
    public void start(String[] args) {
        INSTANCE = this;
        logger.log("// Starting RE:Minecraft " + VERSION + " \\\\");
        // TODO register commands
        
    }

}
