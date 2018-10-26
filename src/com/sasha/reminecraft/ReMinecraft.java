package com.sasha.reminecraft;

import com.github.steveice10.mc.auth.exception.request.RequestException;
import com.github.steveice10.mc.auth.service.AuthenticationService;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.event.session.SessionListener;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;
import com.sasha.eventsys.SimpleEventManager;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.client.children.ChildReClient;
import com.sasha.reminecraft.command.game.TestCommand;
import com.sasha.reminecraft.command.terminal.ExitCommand;
import com.sasha.reminecraft.command.terminal.RelaunchCommand;
import com.sasha.reminecraft.util.YML;
import com.sasha.simplecmdsys.SimpleCommandProcessor;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

/**
 * The main Re:Minecraft class, where the majority of essential startup functions will be stored.
 */
public class ReMinecraft {

    private static final Thread shutdownThread = new Thread(() -> ReMinecraft.INSTANCE.stopSoft());

    /**
     * Singleton of this Re:Minecraft
     */
    public static ReMinecraft INSTANCE;
    public static final String DATA_FILE = "ReMinecraft.yml";
    private List<Configuration> configurations = new ArrayList<>();
    public Configuration MAIN_CONFIG = new Configuration(DATA_FILE);

    /**
     * Current software version of Re:Minecraft
     */
    public static final String VERSION = "1.0a";

    public Logger logger = new Logger("RE:Minecraft " + VERSION);
    public Client minecraftClient = null;
    public Server minecraftServer = null;
    public MinecraftProtocol protocol;
    public List<ChildReClient> childClients = new ArrayList<>();
    public LinkedHashMap<ChildReClient, SessionListener> childAdapters = new LinkedHashMap<>();
    private boolean isShuttingDownCompletely = false;
    private boolean isRelaunching = false;

    /**
     * The command line command processor
     */
    public static final SimpleCommandProcessor TERMINAL_CMD_PROCESSOR = new SimpleCommandProcessor("");
    /**
     * The in-game command processor
     */
    public static final SimpleCommandProcessor INGAME_CMD_PROCESSOR = new SimpleCommandProcessor("\\");
    /**
     * The event manager for Re:Minecraft
     */
    public final SimpleEventManager EVENT_BUS = new SimpleEventManager();

    /**
     * Launch Re:Minecraft and and setup the console command system.
     */
    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        Runtime.getRuntime().addShutdownHook(shutdownThread);
        new ReMinecraft().start(args); // start Re:Minecraft before handling console commands
        Scanner scanner = new Scanner(System.in);
        String cmd = scanner.nextLine();
        while (true) {
            TERMINAL_CMD_PROCESSOR.processCommand(cmd);
        }
    }

    public void sendToChildren(Packet pck) {
        INSTANCE.childClients.stream()
                .filter(ChildReClient::isPlaying)
                .forEach(client -> client.getSession().send(pck));
    }

    /**
     * Launch (or relaunch) Re:Minecraft
     */
    public void start(String[] args) throws InstantiationException, IllegalAccessException {
        INSTANCE = this;
        logger.log("Starting RE:Minecraft " + VERSION + "");
        this.registerCommands();
        this.registerConfigurations();
        configurations.forEach(Configuration::configure); // set config vars
        authenticate(); // log into mc
        minecraftClient = new Client(MAIN_CONFIG.var_remoteServerIp,
                MAIN_CONFIG.var_remoteServerPort,
                protocol,
                new TcpSessionFactory(/*todo proxies?*/));
        minecraftClient.getSession().addListener(new ReClient());
        this.logger.log("Connecting...");
        minecraftClient.getSession().connect(true); // connect to the remote server
        this.logger.log("Connected!");
    }

    /**
     * Authenticate with Mojang, first via session token, then via email/password
     */
    public AuthenticationService authenticate() {
        if (!MAIN_CONFIG.var_sessionId.equalsIgnoreCase("[no default]")) {
            try {
                // try authing with session id first, since it [appears] to be present
                ReMinecraft.INSTANCE.logger.log("Attempting to log in with session token");
                var authServ = new AuthenticationService(MAIN_CONFIG.var_clientId, Proxy.NO_PROXY);
                authServ.setUsername(MAIN_CONFIG.var_mojangEmail);
                authServ.setAccessToken(MAIN_CONFIG.var_sessionId);
                authServ.login();
                protocol = new MinecraftProtocol(authServ.getSelectedProfile(), MAIN_CONFIG.var_clientId, authServ.getAccessToken());
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
            var authServ = new AuthenticationService(MAIN_CONFIG.var_clientId, Proxy.NO_PROXY);
            authServ.setUsername(MAIN_CONFIG.var_mojangEmail);
            authServ.setPassword(MAIN_CONFIG.var_mojangPassword);
            authServ.login();
            protocol = new MinecraftProtocol(authServ.getSelectedProfile(), MAIN_CONFIG.var_clientId, authServ.getAccessToken());
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

    public boolean areChildrenConnected() {
        return !childClients.isEmpty();
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
    public File getDataFile(String s) {
        File file = new File(s);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private void registerCommands() throws InstantiationException, IllegalAccessException {
        TERMINAL_CMD_PROCESSOR.register(ExitCommand.class);
        TERMINAL_CMD_PROCESSOR.register(RelaunchCommand.class);
        INGAME_CMD_PROCESSOR.register(TestCommand.class);
    }

    private void registerConfigurations() {
        configurations.add(MAIN_CONFIG);
    }

    /**
     * Stop and close RE:Minecraft
     */
    public void stop() {
        isShuttingDownCompletely = true;
        Runtime.getRuntime().removeShutdownHook(shutdownThread);
        logger.log("Stopping RE:Minecraft...");
        minecraftServer.getSessions().forEach(session -> session.disconnect("RE:Minecraft is shutting down!", true));
        if (minecraftClient != null && minecraftClient.getSession().isConnected())
            minecraftClient.getSession().disconnect("RE:Minecraft is shutting down...", true);
        logger.log("Stopped RE:Minecraft...");
        System.exit(0);
    }

    public void stopSoft() {
        isShuttingDownCompletely = true;
        logger.log("Stopping RE:Minecraft...");
        minecraftServer.getSessions().forEach(session -> session.disconnect("RE:Minecraft is shutting down!", true));
        if (minecraftClient != null && minecraftClient.getSession().isConnected())
            minecraftClient.getSession().disconnect("RE:Minecraft is shutting down...", true);
        logger.log("Stopped RE:Minecraft...");
    }

    /**
     * Invoked if the player gets kicked from the remote server
     */
    public void reLaunch() {
        if (isShuttingDownCompletely) return;
        if (isRelaunching) return;
        isRelaunching = true;
        if (minecraftClient.getSession().isConnected())
            minecraftClient.getSession().disconnect("RE:Minecraft is restarting!");
        minecraftServer.getSessions().forEach(session -> session.disconnect("RE:Minecraft is restarting!", true));
        ReClient.ReClientCache.chunkCache.clear();
        ReClient.ReClientCache.entityCache.clear();
        ReClient.ReClientCache.player = null;
        ReClient.ReClientCache.posX = 0;
        ReClient.ReClientCache.posY = 0;
        ReClient.ReClientCache.posZ = 0;
        ReClient.ReClientCache.entityId = 0;
        ReClient.ReClientCache.playerListEntries.clear();
        new Thread(() -> {
            for (int i = MAIN_CONFIG.var_reconnectDelaySeconds; i > 0; i--) {
                ReMinecraft.INSTANCE.logger.logWarning("Reconnecting in " + i + " seconds");
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownThread);
                ReMinecraft.main(new String[]{});
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
