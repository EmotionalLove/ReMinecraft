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
import com.sasha.reminecraft.api.RePlugin;
import com.sasha.reminecraft.api.RePluginLoader;
import com.sasha.reminecraft.api.event.MojangAuthenticateEvent;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.client.children.ChildReClient;
import com.sasha.reminecraft.command.game.AboutCommand;
import com.sasha.reminecraft.command.game.PluginsCommand;
import com.sasha.reminecraft.command.terminal.ExitCommand;
import com.sasha.reminecraft.command.terminal.RelaunchCommand;
import com.sasha.reminecraft.util.YML;
import com.sasha.simplecmdsys.SimpleCommandProcessor;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
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
    public static final String DATA_FILE = "ReMinecraft";
    public List<Configuration> configurations = new ArrayList<>();
    public Configuration MAIN_CONFIG = new Configuration(DATA_FILE);

    /**
     * Current software version of Re:Minecraft
     */
    public static final String VERSION = "1.1";

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
    public static void main(String[] args) throws IllegalAccessException, InstantiationException, IOException {
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
    public void start(String[] args) throws InstantiationException, IllegalAccessException, IOException {
        INSTANCE = this;
        new ReClient.ReClientCache();
        logger.log("Starting RE:Minecraft " + VERSION + " for Minecraft 1.12.2");
        RePluginLoader loader = new RePluginLoader();
        loader.preparePlugins(loader.findPlugins());
        loader.loadPlugins();
        this.registerCommands();
        this.registerConfigurations();
        configurations.forEach(Configuration::configure); // set config vars
        Proxy proxy = Proxy.NO_PROXY;
        if (!MAIN_CONFIG.var_socksProxy.equalsIgnoreCase("[no default]") && MAIN_CONFIG.var_socksPort != -1) {
            proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(InetAddress.getByName(MAIN_CONFIG.var_socksProxy), MAIN_CONFIG.var_socksPort));
        }
        AuthenticationService service = authenticate(proxy);// log into mc
        if (service != null) {
            minecraftClient = new Client(MAIN_CONFIG.var_remoteServerIp,
                    MAIN_CONFIG.var_remoteServerPort,
                    protocol,
                    new TcpSessionFactory(proxy));
            minecraftClient.getSession().addListener(new ReClient());
            this.logger.log("Connecting...");
            RePluginLoader.getPluginList().forEach(RePlugin::onPluginEnable);
            minecraftClient.getSession().connect(true); // connect to the remote server
            this.logger.log("Connected!");
        }
    }

    /**
     * Authenticate with Mojang, first via session token, then via email/password
     */
    public AuthenticationService authenticate(Proxy proxy) {
        if (!MAIN_CONFIG.var_sessionId.equalsIgnoreCase("[no default]")) {
            try {
                MojangAuthenticateEvent.Pre event = new MojangAuthenticateEvent.Pre(MojangAuthenticateEvent.Method.SESSIONID);
                this.EVENT_BUS.invokeEvent(event);
                if (event.isCancelled()) {
                    return null;
                }
                // try authing with session id first, since it [appears] to be present
                ReMinecraft.INSTANCE.logger.log("Attempting to log in with session token");
                AuthenticationService authServ = new AuthenticationService(MAIN_CONFIG.var_clientId, proxy);
                authServ.setUsername(MAIN_CONFIG.var_mojangEmail);
                authServ.setAccessToken(MAIN_CONFIG.var_sessionId);
                authServ.login();
                protocol = new MinecraftProtocol(authServ.getSelectedProfile(), MAIN_CONFIG.var_clientId,authServ.getAccessToken());
                updateToken(authServ.getAccessToken());
                MojangAuthenticateEvent.Post postEvent = new MojangAuthenticateEvent.Post(true);
                this.EVENT_BUS.invokeEvent(postEvent);
                ReMinecraft.INSTANCE.logger.log("Logged in as " + authServ.getSelectedProfile().getName());
                ReClient.ReClientCache.INSTANCE.playerName = authServ.getSelectedProfile().getName();
                ReClient.ReClientCache.INSTANCE.playerUuid = authServ.getSelectedProfile().getId();
                return authServ;
            } catch (RequestException ex) {
                // the session token is invalid
                MojangAuthenticateEvent.Post postEvent = new MojangAuthenticateEvent.Post(false);
                this.EVENT_BUS.invokeEvent(postEvent);
                ReMinecraft.INSTANCE.logger.logError("Session token was invalid!");
            }
        }
        // log in normally w username and password
        ReMinecraft.INSTANCE.logger.log("Attemping to log in with email and password");
        try {
            MojangAuthenticateEvent.Pre event = new MojangAuthenticateEvent.Pre(MojangAuthenticateEvent.Method.EMAILPASS);
            this.EVENT_BUS.invokeEvent(event);
            if (event.isCancelled()) return null;
            AuthenticationService authServ = new AuthenticationService(MAIN_CONFIG.var_clientId, proxy);
            authServ.setUsername(MAIN_CONFIG.var_mojangEmail);
            authServ.setPassword(MAIN_CONFIG.var_mojangPassword);
            authServ.login();
            protocol = new MinecraftProtocol(authServ.getSelectedProfile(), MAIN_CONFIG.var_clientId, authServ.getAccessToken());
            updateToken(authServ.getAccessToken());
            ReMinecraft.INSTANCE.logger.log("Logged in as " + authServ.getSelectedProfile().getName());
            ReClient.ReClientCache.INSTANCE.playerName = authServ.getSelectedProfile().getName();
            ReClient.ReClientCache.INSTANCE.playerUuid = authServ.getSelectedProfile().getId();
            MojangAuthenticateEvent.Post postEvent = new MojangAuthenticateEvent.Post(true);
            this.EVENT_BUS.invokeEvent(postEvent);
            return authServ;
        } catch (RequestException e) {
            // login completely failed
            MojangAuthenticateEvent.Post postEvent = new MojangAuthenticateEvent.Post(false);
            this.EVENT_BUS.invokeEvent(postEvent);
            ReMinecraft.INSTANCE.logger.logError(e.getMessage());
            ReMinecraft.INSTANCE.logger.logError("Could not login with Mojang.");
            Scanner scanner = new Scanner(System.in);
            System.out.print("mojang email > ");
            String email = scanner.nextLine();
            System.out.print("\n");
            System.out.print("mojang password > ");
            String password = scanner.nextLine();
            //scanner.close();
            MAIN_CONFIG.var_mojangEmail = email;
            MAIN_CONFIG.var_mojangPassword = password;
            MAIN_CONFIG.save();
            authenticate(proxy);
        }
        return null;
    }

    /**
     * Update the session token inside ReMinecraft.yml
     */
    private void updateToken(String token) {
        MAIN_CONFIG.var_sessionId = token;
    }

    public boolean areChildrenConnected() {
        return !childClients.isEmpty();
    }

    public File getDataFile() {
        File file = new File(DATA_FILE + ".yml");
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
        File file = new File(s + ".yml");
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
        INGAME_CMD_PROCESSOR.register(PluginsCommand.class);
        INGAME_CMD_PROCESSOR.register(AboutCommand.class);
        RePluginLoader.getPluginList().forEach(RePlugin::registerCommands);
    }

    public boolean processInGameCommand(String s) {
        if (!s.startsWith("\\")) {
            return false;
        }
        INGAME_CMD_PROCESSOR.processCommand(s);
        return true;
    }

    private void registerConfigurations() {
        configurations.add(MAIN_CONFIG);
        RePluginLoader.getPluginList().forEach(RePlugin::registerConfig);
    }

    /**
     * Stop and close RE:Minecraft
     */
    public void stop() {
        if (isShuttingDownCompletely) return;
        isShuttingDownCompletely = true;
        configurations.forEach(Configuration::save);
        Runtime.getRuntime().removeShutdownHook(shutdownThread);
        logger.log("Stopping RE:Minecraft...");
        RePluginLoader.shutdownPlugins();
        RePluginLoader.getPluginList().clear();
        if (minecraftServer != null) minecraftServer.getSessions().forEach(session -> session.disconnect("RE:Minecraft is shutting down!", true));
        if (minecraftClient != null && minecraftClient.getSession().isConnected())
            minecraftClient.getSession().disconnect("RE:Minecraft is shutting down...", true);
        logger.log("Stopped RE:Minecraft...");
        System.exit(0);
    }

    public void stopSoft() {
        if (isShuttingDownCompletely) return;
        isShuttingDownCompletely = true;
        configurations.forEach(Configuration::save);
        logger.log("Stopping RE:Minecraft...");
        RePluginLoader.shutdownPlugins();
        RePluginLoader.getPluginList().clear();
        if (minecraftServer != null) minecraftServer.getSessions().forEach(session -> session.disconnect("RE:Minecraft is shutting down!", true));
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
        configurations.forEach(Configuration::save);
        RePluginLoader.shutdownPlugins();
        RePluginLoader.getPluginList().clear();
        if (minecraftClient != null && minecraftClient.getSession().isConnected())
            minecraftClient.getSession().disconnect("RE:Minecraft is restarting!");
        if (minecraftServer != null) minecraftServer.getSessions().forEach(session -> session.disconnect("RE:Minecraft is restarting!", true));
        ReClient.ReClientCache.INSTANCE.chunkCache.clear();
        ReClient.ReClientCache.INSTANCE.entityCache.clear();
        ReClient.ReClientCache.INSTANCE.player = null;
        ReClient.ReClientCache.INSTANCE.posX = 0;
        ReClient.ReClientCache.INSTANCE.posY = 0;
        ReClient.ReClientCache.INSTANCE.posZ = 0;
        ReClient.ReClientCache.INSTANCE.entityId = 0;
        ReClient.ReClientCache.INSTANCE.playerListEntries.clear();
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
            } catch (IllegalAccessException | InstantiationException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
