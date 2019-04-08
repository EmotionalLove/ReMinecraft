package com.sasha.reminecraft.command.terminal;

import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.RePluginLoader;
import com.sasha.simplecmdsys.SimpleCommand;

import java.io.IOException;

public class ReloadCommand extends SimpleCommand {

    public ReloadCommand() {
        super("reload");
    }

    @Override
    public void onCommand() {
        ReMinecraft.LOGGER.logWarning("Reloading all plugins. This can cause plugins to break, or cause RE:Minecraf to break.");
        ReMinecraft.LOGGER.logWarning("Please fully relaunch RE:Minecraft as soon it is convenient");
        //
        ReMinecraft.LOGGER.log("Disabling all loaded plugins...");
        RePluginLoader.disablePlugins();
        ReMinecraft.LOGGER.log("Shutting down all plugins...");
        RePluginLoader.shutdownPlugins();
        ReMinecraft.LOGGER.log("Reloading all JAR files...");
        RePluginLoader loader = new RePluginLoader();
        try {
            loader.preparePlugins(loader.findPlugins());
        } catch (IOException e) {
            ReMinecraft.LOGGER.logError("Failure loading jar files...");
            e.printStackTrace();
            return;
        }
        ReMinecraft.LOGGER.log("Initialising all loaded plugins...");
        RePluginLoader.initPlugins();
        ReMinecraft.LOGGER.log("Enabling all loaded plugins...");
    }
}
