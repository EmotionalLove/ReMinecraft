package com.sasha.reminecraft;

import com.github.steveice10.mc.auth.exception.request.RequestException;
import com.github.steveice10.mc.auth.service.AuthenticationService;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;
import com.sasha.reminecraft.client.ReListener;
import com.sasha.reminecraft.client.children.ChildReClient;
import com.sasha.reminecraft.command.ExitCommand;
import com.sasha.reminecraft.util.YML;
import com.sasha.simplecmdsys.SimpleCommandProcessor;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
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
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ReMinecraft.INSTANCE.stopSoft();
        }));
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
        authenticate(); // log into mc
        minecraftClient = new Client(Configuration.var_remoteServerIp,
                Configuration.var_remoteServerPort,
                protocol,
                new TcpSessionFactory(/*todo proxies?*/));
        minecraftClient.getSession().addListener(new ReListener());
        this.logger.log("Connecting...");
        minecraftClient.getSession().connect(true); // connect to the remote server
    }

    /**
     * Authenticate with Mojang, first via session token, then via email/password
     */
    public AuthenticationService authenticate() {
        if (!Configuration.var_sessionId.equalsIgnoreCase("[no default]")) {
            try {
                // try authing with session id first, since it [appears] to be present
                ReMinecraft.INSTANCE.logger.log("Attempting to log in with session token");
                var authServ = new AuthenticationService(Configuration.var_clientId, Proxy.NO_PROXY);
                authServ.setUsername(Configuration.var_mojangEmail);
                authServ.setAccessToken(Configuration.var_sessionId);
                authServ.login();
                protocol = new MinecraftProtocol(authServ.getSelectedProfile(), authServ.getAccessToken());
                updateToken(authServ.getAccessToken());
                ReMinecraft.INSTANCE.logger.log("Logged in as " + authServ.getSelectedProfile().getName());
                return authServ;
            } catch (RequestException ex) {
                // the session token is invalid
                ReMinecraft.INSTANCE.logger.logError("Session token was invalid!");
            }
        }
        // log in normally w username and password
        ReMinecraft.INSTANCE.logger.log("Attemping to log in with email and password");
        try {
            var authServ = new AuthenticationService(Configuration.var_clientId, Proxy.NO_PROXY);
            authServ.setUsername(Configuration.var_mojangEmail);
            authServ.setPassword(Configuration.var_mojangPassword);
            authServ.login();
            protocol = new MinecraftProtocol(authServ.getSelectedProfile(), authServ.getAccessToken());
            updateToken(authServ.getAccessToken());
            ReMinecraft.INSTANCE.logger.log("Logged in as " + authServ.getSelectedProfile().getName());
        } catch (RequestException e) {
            // login completely failed
            e.printStackTrace();
            ReMinecraft.INSTANCE.logger.logError("Could not login with Mojang.");
            ReMinecraft.INSTANCE.stop();
        }
        return null;
    }

    /**
     * Update the session token inside ReMinecraft.yml
     */
    private void updateToken(String token) {
        YML yml = new YML(getDataFile());
        yml.set("sessionId", token);
        yml.save();
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
        if (minecraftClient != null && minecraftClient.getSession().isConnected())
            minecraftClient.getSession().disconnect("RE:Minecraft is shutting down...", true);
        logger.log("Stopped RE:Minecraft...");
        System.exit(0);
    }
    public void stopSoft() {
        logger.log("Stopping RE:Minecraft...");
        if (minecraftClient != null && minecraftClient.getSession().isConnected())
            minecraftClient.getSession().disconnect("RE:Minecraft is shutting down...", true);
        logger.log("Stopped RE:Minecraft...");
    }

}
