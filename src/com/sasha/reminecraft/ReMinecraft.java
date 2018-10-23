package com.sasha.reminecraft;

import com.github.steveice10.mc.auth.service.AuthenticationService;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.Session;
import com.sasha.reminecraft.command.ExitCommand;
import com.sasha.reminecraft.util.YML;
import com.sasha.simplecmdsys.SimpleCommandProcessor;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The main Re:Minecraft class, where the majority of essential startup functions will be stored.
 */
public class ReMinecraft {

    /**
     * Singleton of this Re:Minecraft
     */
    public static ReMinecraft INSTANCE;
    public static final SimpleCommandProcessor COMMAND_PROCESSOR = new SimpleCommandProcessor("");
    public static final String DATA_FILE = "ReMinecraft.yml";

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
    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        new ReMinecraft().start(args); // start Re:Minecraft before handling console commands
        Scanner scanner = new Scanner(System.in);
        String cmd = scanner.nextLine();
        while (true) {
            COMMAND_PROCESSOR.processCommand(cmd);
        }
    }

    /**
     * Launch (or relaunch) Re:Minecraft
     */
    public void start(String[] args) throws InstantiationException, IllegalAccessException {
        INSTANCE = this;
        logger.log("Starting RE:Minecraft " + VERSION + "");
        COMMAND_PROCESSOR.register(ExitCommand.class);
        Configuration.configure(); // set config vars
    }

    /**
     * Authenticate with Mojang, first via session token, then via email/password
     */
    public AuthenticationService authenticate() {
        File file = getDataFile();
        YML parser = new YML(file);
        if (parser.exists("sessionid")) {

        }
        return null;
    }

    public File getDataFile() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * Stop and close RE:Minecraft
     */
    public void stop() {
        logger.log("Stopping RE:Minecraft...");
        logger.log("Stopped RE:Minecraft...");
        System.exit(0);
    }

}
