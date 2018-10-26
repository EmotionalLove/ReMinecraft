package com.sasha.reminecraft.api;

import java.util.ArrayList;
import java.util.List;

public class RePluginLoader {

    private static List<RePlugin> pluginList = new ArrayList<>();

    public static List<RePlugin> getPluginList() {
        return pluginList;
    }
}
