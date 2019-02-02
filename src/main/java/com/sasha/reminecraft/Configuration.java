package com.sasha.reminecraft;

import com.sasha.reminecraft.api.exception.ReMinecraftPluginConfigurationException;
import com.sasha.reminecraft.util.ReUtil;
import com.sasha.reminecraft.util.YML;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Holds all of the configuration values
 */
public class Configuration {

    private String configName;

    /**
     * The global configuration vars
     * Format:
     * #@ConfigSetting public [Type] var_[name] = [defualt value]
     */
    @ConfigSetting
    public String var_sessionId = null;
    @ConfigSetting
    public String var_clientId = UUID.randomUUID().toString();
    @ConfigSetting
    public String var_mojangEmail = null;
    @ConfigSetting
    public String var_mojangPassword = null;
    @ConfigSetting
    public boolean var_cracked = false;
    @ConfigSetting
    public String var_remoteServerIp = "constantiam.net";
    @ConfigSetting
    public String var_hostServerIp = "0.0.0.0";
    @ConfigSetting
    public int var_remoteServerPort = 25565;
    @ConfigSetting
    public int var_hostServerPort = 25565;
    @ConfigSetting
    public boolean var_onlineModeServer = true;
    @ConfigSetting
    public int var_pingTimeoutSeconds = 30;
    @ConfigSetting
    public boolean var_useWhitelist = false;
    @ConfigSetting
    public ArrayList<String> var_whitelistServer = new ArrayList<>();
    @ConfigSetting
    public String var_messageOfTheDay = "&dRE:Minecraft &7" + ReMinecraft.VERSION;
    @ConfigSetting
    public int var_reconnectDelaySeconds = 5;
    @ConfigSetting
    public boolean var_authWithoutProxy = true;
    @ConfigSetting
    public String var_socksProxy = null;
    @ConfigSetting
    public int var_socksPort = -1;

    {
        var_whitelistServer.add("Phi_Phi");
        var_whitelistServer.add("Color");
        var_whitelistServer.add("086");
        var_whitelistServer.add("minecart26");
    }

    public Configuration(String configName) {
        this.configName = configName;
    }

    /**
     * Fill the above fields and version the config.
     */
    protected final void configure() {
        try {
            File file = ReUtil.getDataFile(configName);
            YML yml = new YML(file);
            for (Field declaredField : this.getClass().getDeclaredFields()) {
                if (declaredField.getAnnotation(ConfigSetting.class) == null) continue;
                declaredField.setAccessible(true);
                if (!yml.exists("config-version")) {
                    yml.set("config-version", 0);
                }
                String target = declaredField.getName().replace("var_", "");
                if (!yml.exists(target)) {
                    yml.set(target, declaredField.get(this) == null ? "[no default]" : declaredField.get(this));
                    declaredField.set(this, declaredField.get(this) == null ? "[no default]" : declaredField.get(this));
                    ReMinecraft.LOGGER.log("Created " + target);
                    continue;
                }
                if (declaredField.getType() == float.class) {
                    declaredField.set(this, yml.getFloat(target));
                } else {
                    declaredField.set(this, yml.get(target));
                }
                ReMinecraft.LOGGER.logDebug("Set " + target);
            }
            yml.save();
        } catch (IllegalAccessException ex) {
            ReMinecraftPluginConfigurationException exc = new ReMinecraftPluginConfigurationException("Configuration error while reading " + this.getClass().getSimpleName());
            exc.setStackTrace(ex.getStackTrace());
            throw exc;
        }
    }

    protected final void save() {
        try {
            File file = ReUtil.getDataFile(configName);
            YML yml = new YML(file);
            for (Field declaredField : this.getClass().getDeclaredFields()) {
                if (declaredField.getAnnotation(ConfigSetting.class) == null) continue;
                declaredField.setAccessible(true);
                if (!yml.exists("config-version")) {
                    yml.set("config-version", 0);
                }
                String target = declaredField.getName().replace("var_", "");
                yml.set(target, declaredField.get(this));
                ReMinecraft.LOGGER.logDebug("Saved " + target);
            }
            yml.save();
        } catch (IllegalAccessException ex) {
            ReMinecraftPluginConfigurationException exc = new ReMinecraftPluginConfigurationException("Configuration error while writing " + this.getClass().getSimpleName());
            exc.setStackTrace(ex.getStackTrace());
            throw exc;
        }
    }

    public String getConfigName() {
        return configName;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ConfigSetting {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Removed {

    }
}
