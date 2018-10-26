package com.sasha.reminecraft.api;

import com.sasha.reminecraft.ReMinecraft;

public abstract class RePlugin {

    private String pluginName;
    private String pluginDescription;
    private String[] pluginAuthors;
    private String pluginVersion;

    public abstract void onPluginInit();
    public abstract void onPluginEnable();
    public abstract void onPluginDisable();
    public abstract void registerCommands();
    public abstract void registerConfig();

    public ReMinecraft getReMinecraft() {
        return ReMinecraft.INSTANCE;
    }
}
