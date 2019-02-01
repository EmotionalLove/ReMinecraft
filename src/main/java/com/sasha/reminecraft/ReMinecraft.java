package com.sasha.reminecraft;

import com.github.steveice10.mc.auth.exception.request.RequestException;
import com.github.steveice10.mc.auth.service.AuthenticationService;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.event.session.SessionListener;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;
import com.sasha.eventsys.SimpleEventManager;
import com.sasha.reminecraft.api.RePlugin;
import com.sasha.reminecraft.api.RePluginLoader;
import com.sasha.reminecraft.api.event.MojangAuthenticateEvent;
import com.sasha.reminecraft.client.ChildReClient;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.command.game.AboutCommand;
import com.sasha.reminecraft.command.game.PluginsCommand;
import com.sasha.reminecraft.command.terminal.ExitCommand;
import com.sasha.reminecraft.command.terminal.LoginCommand;
import com.sasha.reminecraft.command.terminal.RelaunchCommand;
import com.sasha.reminecraft.javafx.ReMinecraftGui;
import com.sasha.reminecraft.logging.ILogger;
import com.sasha.reminecraft.logging.impl.JavaFXLogger;
import com.sasha.reminecraft.logging.impl.TerminalLogger;
import com.sasha.simplecmdsys.SimpleCommandProcessor;
import javafx.application.Platform;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.sasha.reminecraft.javafx.ReMinecraftGui.launched;

/**
 * The main Re:Minecraft class, where the majority of essential startup functions will be stored.
 */
public class ReMinecraft implements IReMinecraft {

    public static final String DATA_FILE = "ReMinecraft";
    /**
     * Current software version of Re:Minecraft
     */
    public static String VERSION = "2.1";
    /**
     * The command line command processor
     */
    public static final SimpleCommandProcessor TERMINAL_CMD_PROCESSOR = new SimpleCommandProcessor("");
    /**
     * The in-game command processor
     */
    public static final SimpleCommandProcessor INGAME_CMD_PROCESSOR = new SimpleCommandProcessor("\\");
    /**
     * Singleton of this Re:Minecraft
     */
    public static ReMinecraft INSTANCE;

    /**
     * Whether the current instance of reminecraft is using the JavaFX gui or not
     */
    public static boolean isUsingJavaFXGui = true;
    /**
     * The JLine terminal instance
     */
    public static Terminal terminal;
    /**
     * The Jline reader instance
     */
    public static LineReader reader;

    public static ILogger LOGGER;
    /**
     * The args from when we first started the program
     */
    public static String[] args = new String[]{};

    private static final Thread shutdownThread = new Thread(() -> ReMinecraft.INSTANCE.stopSoft());
    /**
     * The event manager for Re:Minecraft
     */
    public final SimpleEventManager EVENT_BUS = new SimpleEventManager();
    public List<Configuration> configurations = new ArrayList<>();
    public Configuration MAIN_CONFIG = new Configuration(DATA_FILE);

    public Client minecraftClient = null;
    public Server minecraftServer = null;
    public MinecraftProtocol protocol;
    public List<ChildReClient> childClients = new ArrayList<>();
    public LinkedHashMap<ChildReClient, SessionListener> childAdapters = new LinkedHashMap<>();
    private static RePluginLoader loader;
    private boolean isShuttingDownCompletely = false;
    private boolean isRelaunching = false;

    /**
     * Launch Re:Minecraft and and setup the console command system.
     */
    public static void main(String[] args) throws IOException {
        ReMinecraft.args = args;
        isUsingJavaFXGui = true;
        if (args.length != 0) {
            if (args[0].toLowerCase().replace("-", "").equals("nogui")) {
                isUsingJavaFXGui = false;
            }
        }
        if (!isUsingJavaFXGui) {
            terminal = TerminalBuilder.builder().name("RE:Minecraft").system(true).build();
            reader = LineReaderBuilder.builder().terminal(terminal).build();
            LOGGER = new TerminalLogger("RE:Minecraft " + VERSION);
        } else {
            LOGGER = new JavaFXLogger("RE:Minecraft " + VERSION);
            if (!launched) new Thread(() -> new ReMinecraftGui().startLaunch()).start();
        }
        Runtime.getRuntime().addShutdownHook(shutdownThread);
        loader = new RePluginLoader();
        loader.preparePlugins(loader.findPlugins());
        loader.loadPlugins();
        Thread thread = new Thread(() -> {
            new ReMinecraft().start(args, false);
        }); // start Re:Minecraft before handling console commands
        if (!isUsingJavaFXGui) thread.run();
        else thread.start();
        while (!isUsingJavaFXGui) {
            try {
                String cmd = reader.readLine(null, null, "> ");
                TERMINAL_CMD_PROCESSOR.processCommand(cmd.trim().replace("> ", ""));
            } catch (UserInterruptException | IllegalStateException | EndOfFileException ex) {
                ReMinecraft.INSTANCE.stop();
                return;
            }
        }
    }

    @Override
    public void sendFromClient(Packet pck) {
        if (minecraftClient == null ||
                !minecraftClient.getSession().isConnected() ||
                ((MinecraftProtocol)minecraftClient.getSession().getPacketProtocol()).getSubProtocol() != SubProtocol.GAME) {
            return;
        }
        minecraftClient.getSession().send(pck);
    }

    @Override
    public void sendToChildren(Packet pck) {
        INSTANCE.childClients.stream()
                .filter(ChildReClient::isPlaying)
                .forEach(client -> client.getSession().send(pck));
    }

    /**
     * Launch (or relaunch) Re:Minecraft
     */
    @Override
    public void start(String[] args, boolean restart) {
        isRelaunching = false;
        isShuttingDownCompletely = false;
        try {
            INSTANCE = this;
            new ReClient.ReClientCache();
            LOGGER.log("Starting RE:Minecraft " + VERSION + " for Minecraft " + MinecraftConstants.GAME_VERSION);
            this.registerCommands();
            this.registerConfigurations();
            configurations.forEach(Configuration::configure); // set config vars
            if (!restart) RePluginLoader.initPlugins();
            Proxy proxy = Proxy.NO_PROXY;
            if (MAIN_CONFIG.var_socksProxy != null && !MAIN_CONFIG.var_socksProxy.equalsIgnoreCase("[no default]") && MAIN_CONFIG.var_socksPort != -1) {
                proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(InetAddress.getByName(MAIN_CONFIG.var_socksProxy), MAIN_CONFIG.var_socksPort));
            }
            //if (isUsingJavaFXGui) Platform.runLater(ReMinecraftGui::refreshConfigurationEntries);
            AuthenticationService service = authenticate(MAIN_CONFIG.var_authWithoutProxy ? Proxy.NO_PROXY : proxy);// log into mc
            if (service != null) {
                minecraftClient = new Client(MAIN_CONFIG.var_remoteServerIp,
                        MAIN_CONFIG.var_remoteServerPort,
                        protocol,
                        new TcpSessionFactory(proxy));
                minecraftClient.getSession().addListener(new ReClient());
                LOGGER.log("Connecting...");
                minecraftClient.getSession().connect(true); // connect to the remote server
                LOGGER.log("Connected!");
                RePluginLoader.enablePlugins();
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.logError("A SEVERE EXCEPTION OCCURRED WHILST STARTING RE:MINECRAFT");
        }
    }

    /**
     * Authenticate with Mojang, first via session token, then via email/password
     */
    @Override
    public AuthenticationService authenticate(Proxy proxy) {
        if (!MAIN_CONFIG.var_sessionId.equalsIgnoreCase("[no default]")) {
            try {
                MojangAuthenticateEvent.Pre event = new MojangAuthenticateEvent.Pre(MojangAuthenticateEvent.Method.SESSIONID);
                this.EVENT_BUS.invokeEvent(event);
                if (event.isCancelled()) {
                    return null;
                }
                // try authing with session id first, since it [appears] to be present
                ReMinecraft.LOGGER.log("Attempting to log in with session token");
                AuthenticationService authServ = new AuthenticationService(MAIN_CONFIG.var_clientId, proxy);
                authServ.setUsername(MAIN_CONFIG.var_mojangEmail);
                authServ.setAccessToken(MAIN_CONFIG.var_sessionId);
                authServ.login();
                protocol = new MinecraftProtocol(authServ.getSelectedProfile(), MAIN_CONFIG.var_clientId, authServ.getAccessToken());
                updateToken(authServ.getAccessToken());
                MojangAuthenticateEvent.Post postEvent = new MojangAuthenticateEvent.Post(MojangAuthenticateEvent.Method.SESSIONID, true);
                this.EVENT_BUS.invokeEvent(postEvent);
                ReMinecraft.LOGGER.log("Logged in as " + authServ.getSelectedProfile().getName());
                ReClient.ReClientCache.INSTANCE.playerName = authServ.getSelectedProfile().getName();
                ReClient.ReClientCache.INSTANCE.playerUuid = authServ.getSelectedProfile().getId();
                return authServ;
            } catch (RequestException ex) {
                // the session token is invalid
                MojangAuthenticateEvent.Post postEvent = new MojangAuthenticateEvent.Post(MojangAuthenticateEvent.Method.SESSIONID, false);
                this.EVENT_BUS.invokeEvent(postEvent);
                ReMinecraft.LOGGER.logError("Session token was invalid!");
            }
        }
        // log in normally w username and password
        ReMinecraft.LOGGER.log("Attemping to log in with email and password");
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
            ReMinecraft.LOGGER.log("Logged in as " + authServ.getSelectedProfile().getName());
            ReClient.ReClientCache.INSTANCE.playerName = authServ.getSelectedProfile().getName();
            ReClient.ReClientCache.INSTANCE.playerUuid = authServ.getSelectedProfile().getId();
            MojangAuthenticateEvent.Post postEvent = new MojangAuthenticateEvent.Post(MojangAuthenticateEvent.Method.EMAILPASS, true);
            this.EVENT_BUS.invokeEvent(postEvent);
            return authServ;
        } catch (RequestException e) {
            // login completely failed
            MojangAuthenticateEvent.Post postEvent = new MojangAuthenticateEvent.Post(MojangAuthenticateEvent.Method.EMAILPASS, false);
            this.EVENT_BUS.invokeEvent(postEvent);
            ReMinecraft.LOGGER.logError(e.getMessage());
            ReMinecraft.LOGGER.logError("Could not login with Mojang.");
            if (postEvent.isCancelled()) {
                return null;
            }
            this.reLaunch();
        }
        return null;
    }

    /**
     * Update the session token inside ReMinecraft.yml
     */
    @Override
    public void updateToken(String token) {
        MAIN_CONFIG.var_sessionId = token;
    }

    @Override
    public boolean areChildrenConnected() {
        return !childClients.isEmpty();
    }

    @Override
    public void registerCommands() {
        TERMINAL_CMD_PROCESSOR.getCommandRegistry().clear();
        INGAME_CMD_PROCESSOR.getCommandRegistry().clear();
        try {
            TERMINAL_CMD_PROCESSOR.register(ExitCommand.class);
            TERMINAL_CMD_PROCESSOR.register(RelaunchCommand.class);
            INGAME_CMD_PROCESSOR.register(PluginsCommand.class);
            INGAME_CMD_PROCESSOR.register(AboutCommand.class);
            TERMINAL_CMD_PROCESSOR.register(LoginCommand.class);
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        RePluginLoader.getPluginList().forEach(RePlugin::registerCommands);
    }

    @Override
    public boolean processInGameCommand(String s) {
        if (!s.startsWith("\\")) {
            return false;
        }
        INGAME_CMD_PROCESSOR.processCommand(s);
        return true;
    }

    @Override
    public void registerConfigurations() {
        configurations.add(MAIN_CONFIG);
        RePluginLoader.getPluginList().forEach(RePlugin::registerConfig);
    }

    /**
     * Stop and close RE:Minecraft
     */
    @Override
    public void stop() {
        if (isShuttingDownCompletely) return;
        isShuttingDownCompletely = true;
        configurations.forEach(Configuration::save);
        Runtime.getRuntime().removeShutdownHook(shutdownThread);
        LOGGER.log("Stopping RE:Minecraft...");
        RePluginLoader.shutdownPlugins();
        RePluginLoader.getPluginList().clear();
        if (minecraftServer != null) {
            minecraftServer.getSessions().forEach(session -> session.disconnect("RE:Minecraft is shutting down!", true));
            minecraftServer.close(true);
        }
        if (minecraftClient != null && minecraftClient.getSession().isConnected())
            minecraftClient.getSession().disconnect("RE:Minecraft is shutting down...", true);
        LOGGER.log("Stopped RE:Minecraft...");
        System.exit(0);
    }

    @Override
    public void stopSoft() {
        if (isShuttingDownCompletely) return;
        isShuttingDownCompletely = true;
        configurations.forEach(Configuration::save);
        LOGGER.log("Stopping RE:Minecraft...");
        RePluginLoader.shutdownPlugins();
        RePluginLoader.getPluginList().clear();
        if (minecraftServer != null) {
            minecraftServer.getSessions().forEach(session -> session.disconnect("RE:Minecraft is shutting down!", true));
            minecraftServer.close(true);
        }
        if (minecraftClient != null && minecraftClient.getSession().isConnected())
            minecraftClient.getSession().disconnect("RE:Minecraft is shutting down...", true);
        LOGGER.log("Stopped RE:Minecraft...");
    }

    /**
     * Invoked if the player gets kicked from the remote server
     */
    @Override
    public void reLaunch() {
        if (isShuttingDownCompletely) return;
        if (isRelaunching) return;
        isRelaunching = true;
        configurations.forEach(Configuration::save);
        RePluginLoader.disablePlugins();
        RePluginLoader.getPluginList().clear();
        if (minecraftClient != null && minecraftClient.getSession().isConnected())
            minecraftClient.getSession().disconnect("RE:Minecraft is restarting!");
        if (minecraftServer != null) {
            minecraftServer.getSessions().forEach(session -> session.disconnect("RE:Minecraft is restarting!", true));
            minecraftServer.close(true);
        }
        ReClient.ReClientCache.INSTANCE.chunkCache.clear();
        ReClient.ReClientCache.INSTANCE.entityCache.clear();
        final int[] i = {-1};
        new Thread(() -> {
            synchronized (ReMinecraft.INSTANCE) {
                for (i[0] = MAIN_CONFIG.var_reconnectDelaySeconds; i[0] >= 0; i[0]--) {
                    ReMinecraft.LOGGER.logWarning("Reconnecting in " + i[0] + " seconds");
                    try {
                        ReMinecraft.INSTANCE.wait(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return;
            }
        }).start();
        while (i[0] == -1 || i[0] != 0) {
            try {
                ReMinecraft.INSTANCE.notify();
            } catch (IllegalMonitorStateException ignored) {
            }
        }
        Runtime.getRuntime().removeShutdownHook(shutdownThread);
        ReMinecraft.INSTANCE.start(ReMinecraft.args, true);
    }
}
