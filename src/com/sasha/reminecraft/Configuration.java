package com.sasha.reminecraft;

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
public abstract class Configuration {

    /**
     * The global configuration vars
     * Format:
     * #@ConfigSetting public static [Type] var_[name] = [defualt value]
     */
    @ConfigSetting public static String var_sessionId = null;
    @ConfigSetting public static String var_clientId = UUID.randomUUID().toString();
    @ConfigSetting public static String var_mojangEmail = null;
    @ConfigSetting public static String var_mojangPassword = null;
    @ConfigSetting public static boolean var_cracked = false;
    @ConfigSetting public static String var_remoteServerIp = "constantiam.net";
    @ConfigSetting public static String var_hostServerIp = "0.0.0.0";
    @ConfigSetting public static int var_remoteServerPort = 25565;
    @ConfigSetting public static int var_hostServerPort = 25565;
    @ConfigSetting public static boolean var_onlineModeServer = true;
    @ConfigSetting public static String var_messageOfTheDay = "\247dRE:Minecraft \2477" + ReMinecraft.VERSION;
    @ConfigSetting public static int var_reconnectDelaySeconds = 5;

    /**
     * Fill the above fields and version the config.
     */
    public static void configure() throws IllegalAccessException {
        File file = ReMinecraft.INSTANCE.getDataFile();
        YML yml = new YML(file);
        for (Field declaredField : Configuration.class.getDeclaredFields()) {
            if (declaredField.getAnnotation(ConfigSetting.class) == null) continue;
            if (!yml.exists("config-version")) {
                yml.set("config-version", 0);
            }
            var target = declaredField.getName().replace("var_", "");
            if (!yml.exists(target)) {
                yml.set(target, declaredField.get(null) == null ? "[no default]" : declaredField.get(null));
                declaredField.set(null, declaredField.get(null) == null ? "[no default]" : declaredField.get(null));
                ReMinecraft.INSTANCE.logger.log("Created " + target);
                continue;
            }
            declaredField.set(null, yml.get(target));
            ReMinecraft.INSTANCE.logger.log("Set " + target);
        }
        yml.save();
    }
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface ConfigSetting {

    }
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Removed {

    }
}
