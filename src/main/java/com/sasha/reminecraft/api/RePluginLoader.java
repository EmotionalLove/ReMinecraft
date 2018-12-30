package com.sasha.reminecraft.api;

import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.logging.ILogger;
import com.sasha.reminecraft.logging.impl.JavaFXLogger;
import com.sasha.reminecraft.logging.impl.TerminalLogger;
import com.sasha.reminecraft.util.YML;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class RePluginLoader {

    private static final String DIR_NAME = "plugins";
    private static ILogger LOGGER;
    private static LinkedHashMap<File /*jar*/, PluginInfo> theRawPlugins = new LinkedHashMap<>();
    private static List<RePlugin> pluginList = new ArrayList<>();

    public RePluginLoader() {
        if (!ReMinecraft.isUsingJavaFXGui) LOGGER = new TerminalLogger("Plugin Loader");
        else LOGGER = new JavaFXLogger("Plugin Loader");
    }

    public List<File> findPlugins() {
        LOGGER.log("Finding plugins...");
        File dir = new File(DIR_NAME);
        if (!dir.exists()) {
            dir.mkdir();
            LOGGER.log("No plugins were found.");
            return new ArrayList<>();
        }
        if (!dir.isDirectory()) {
            dir.delete();
            dir.mkdir();
            LOGGER.log("No plugins were found.");
            return new ArrayList<>();
        }
        File[] allFiles = dir.listFiles();
        if (allFiles == null) {
            LOGGER.log("No plugins were found.");
            return new ArrayList<>(); // no files?
        }
        List<File> theFiles = new ArrayList<>();
        Arrays.stream(allFiles).filter(fname -> fname.getName().endsWith(".jar")).forEach(theFiles::add);
        LOGGER.log(theFiles.size() + " potential plugins were found!");
        return theFiles;
    }

    public int preparePlugins(List<File> theJarFiles) throws IOException {
        int i = 0;
        for (File file : theJarFiles) {
            JarFile jar = new JarFile(file, true);
            JarEntry entry = jar.getJarEntry("plugin.yml");
            if (entry == null) {
                LOGGER.logError(file.getName() + " is missing it's plugin.yml. This plugin will not load!");
                continue; // invalid plugin
            }
            PluginInfo info = new PluginInfo();
            File tmp = new File("tmp_plugin.yml");
            if (tmp.exists()) {
                tmp.delete();
                tmp.createNewFile();
            }
            FileWriter out = new FileWriter(tmp, true);
            InputStreamReader in = new InputStreamReader(jar.getInputStream(entry));
            BufferedReader buffin = new BufferedReader(in);
            String line;
            while ((line = buffin.readLine()) != null) {
                out.write(line + System.lineSeparator());
            }
            in.close();
            out.close();
            YML yml = new YML(tmp);
            if (!yml.exists("name")) throw new IllegalStateException("Key \"name\" doesn't exit");
            if (!yml.exists("description")) throw new IllegalStateException("Key \"name\" doesn't exit");
            if (!yml.exists("authors")) throw new IllegalStateException("Key \"name\" doesn't exit");
            if (!yml.exists("version")) throw new IllegalStateException("Key \"name\" doesn't exit");
            if (!yml.exists("mainClass")) throw new IllegalStateException("Key \"name\" doesn't exit");
            info.pluginName = yml.getString("name");
            info.pluginDescription = yml.getString("description");
            info.pluginAuthors = yml.getStringList("authors").toArray(new String[0]);
            info.pluginVersion = yml.getString("version");
            info.mainClass = yml.getString("mainClass");
            tmp.delete();
            LOGGER.log("Prepared " + info.pluginName + " " + info.pluginVersion);
            theRawPlugins.put(file, info);
        }
        return i;
    }

    public void loadPlugins() {
        final int[] failed = {0};
        theRawPlugins.forEach((jar, info) -> {
            try {
                URLClassLoader classLoader = new URLClassLoader(new URL[]{jar.toURI().toURL()}, this.getClass().getClassLoader());
                Class clazz = Class.forName(info.mainClass, true, classLoader);
                if (clazz.getSuperclass() == null || clazz.getSuperclass() != RePlugin.class) {
                    //invalid
                    LOGGER.logError(info.pluginName + " has an invalid main class, cannot load.");
                    return;
                }
                RePlugin plugin = (RePlugin) clazz.newInstance();
                plugin.pluginName = info.pluginName;
                plugin.pluginDescription = info.pluginDescription;
                plugin.pluginAuthors = info.pluginAuthors;
                plugin.pluginVersion = info.pluginVersion;
                LOGGER.log(plugin.pluginName + " " + plugin.pluginVersion + " is initialising...");
                plugin.onPluginInit();
                pluginList.add(plugin);
                LOGGER.log(plugin.pluginName + " initialised");
            } catch (Exception e) {
                LOGGER.logError("A severe uncaught exception occurred whilst trying to load " + info.pluginName + "(" + info.mainClass + ")");
                //failed[0]++;
                e.printStackTrace();
            }
        });
        LOGGER.log((getPluginList().size() - failed[0]) + " plugins were successfully loaded");
    }

    public static void shutdownPlugins() {
        getPluginList().forEach(pl -> {
            LOGGER.log("Shutting down " + pl.pluginName);
            try {
                pl.onPluginShutdown();
            } catch (Exception e) {
                LOGGER.logError("A severe uncaught exception occurred whilst trying to disable " + pl.pluginName);
                e.printStackTrace();
            }
        });
    }

    public static void enablePlugins() {
        getPluginList().forEach(pl -> {
            LOGGER.log("Enabling " + pl.pluginName);
            try {
                pl.onPluginEnable();
            } catch (Exception e) {
                LOGGER.logError("A severe uncaught exception occurred whilst trying to disable " + pl.pluginName);
                e.printStackTrace();
            }
        });
    }

    public static void disablePlugins() {
        getPluginList().forEach(pl -> {
            LOGGER.log("Disabling " + pl.pluginName);
            try {
                pl.onPluginDisable();
            } catch (Exception e) {
                LOGGER.logError("A severe uncaught exception occurred whilst trying to disable " + pl.pluginName);
                e.printStackTrace();
            }
        });
    }


    public static List<RePlugin> getPluginList() {
        return pluginList;
    }
}

class PluginInfo {
    protected String pluginName;
    protected String pluginDescription;
    protected String[] pluginAuthors;
    protected String pluginVersion;
    protected String mainClass;
}
