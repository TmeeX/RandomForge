package com.willzcode;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

/**
 * Created by willz on 2018/4/24.
 */
public class PluginDataFactory {
    public static PluginData loadData(JavaPlugin plugin, String name) {
        File file = new File(plugin.getDataFolder(), name + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new PluginData(file, YamlConfiguration.loadConfiguration(file));
    }
}
