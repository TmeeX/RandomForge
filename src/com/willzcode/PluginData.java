package com.willzcode;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by willz on 2018/4/23.
 */
public class PluginData {
    private File file;
    private FileConfiguration config;

    public PluginData(File file, FileConfiguration config) {
        this.file = file;
        this.config = config;
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Collection<String> getKeys(String path) {
        if (path == null || path.equals("")) {
            return config.getKeys(false);
        } else {
            return config.getConfigurationSection(path).getKeys(false);
        }
    }

    public void set(String path, Object object) {
        config.set(path, object);
    }

    public Object get(String path) {
        return config.get(path);
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }
}
