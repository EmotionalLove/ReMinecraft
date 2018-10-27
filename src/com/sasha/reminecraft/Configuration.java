package com.sasha.reminecraft;

import com.sasha.reminecraft.api.exception.ReMinecraftPluginConfigurationException;
import com.sasha.reminecraft.util.YML;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
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
    public String var_messageOfTheDay = "\247dRE:Minecraft \2477" + ReMinecraft.VERSION;
    @ConfigSetting
    public int var_reconnectDelaySeconds = 5;

    public Configuration(String configName) {
        this.configName = configName;
    }

    /**
     * Fill the above fields and version the config.
     */
    protected final void configure() {
        try {
            File file = ReMinecraft.INSTANCE.getDataFile(configName);
            YML yml = new YML(file);
            for (Field declaredField : this.getClass().getDeclaredFields()) {
                if (declaredField.getAnnotation(ConfigSetting.class) == null) continue;
                declaredField.setAccessible(true);
                if (!yml.exists("config-version")) {
                    yml.set("config-version", 0);
                }
                var target = declaredField.getName().replace("var_", "");
                if (!yml.exists(target)) {
                    yml.set(target, declaredField.get(this) == null ? "[no default]" : declaredField.get(this));
                    declaredField.set(this, declaredField.get(this) == null ? "[no default]" : declaredField.get(this));
                    ReMinecraft.INSTANCE.logger.log("Created " + target);
                    continue;
                }
                if (declaredField.getType() == float.class) {
                    declaredField.set(this, yml.getFloat(target));
                }
                else {
                    declaredField.set(this, yml.get(target));
                }
                ReMinecraft.INSTANCE.logger.log("Set " + target);
            }
            yml.save();
        } catch (IllegalAccessException ex) {
            var exc = new ReMinecraftPluginConfigurationException("Configuration error while reading " + this.getClass().getSimpleName());
            exc.setStackTrace(ex.getStackTrace());
            throw exc;
        }
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
