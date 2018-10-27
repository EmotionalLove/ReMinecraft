package com.sasha.reminecraft.api;

import com.sasha.reminecraft.ReMinecraft;

public abstract class RePlugin {

    public String pluginName;
    public String pluginDescription;
    public String[] pluginAuthors;
    public String pluginVersion;

    public abstract void onPluginInit();

    public abstract void onPluginEnable();

    public abstract void onPluginDisable();

    public abstract void registerCommands();

    public abstract void registerConfig();

    public ReMinecraft getReMinecraft() {
        return ReMinecraft.INSTANCE;
    }
}
