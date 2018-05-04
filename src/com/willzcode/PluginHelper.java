package com.willzcode;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by willz on 2018/4/23.
 */
public class PluginHelper {
    public static String getVersion(JavaPlugin plugin) {
        return plugin.getDescription().getVersion();
    }

    public static FileConfiguration loadCheckConfig(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        InputStream in = plugin.getResource("config.yml");
        FileConfiguration resourseCfg = YamlConfiguration.loadConfiguration(new InputStreamReader(in));
        int cfgversion = cfg.getInt("version", 0);
        if (cfgversion < resourseCfg.getInt("version", 0)) {
            plugin.saveResource("config.yml", true);
            plugin.reloadConfig();
            cfg = plugin.getConfig();
        }

        return cfg;
    }

    public static void doEnable(JavaPlugin plugin) {
        Bukkit.getConsoleSender().sendMessage(String.format("§a%s插件v%s已启用！§e开发者：§b§l%s",
                plugin.getDescription().getPrefix(), plugin.getDescription().getVersion(), plugin.getDescription().getAuthors().get(0)));
    }

    public static void doDisable(JavaPlugin plugin) {
        Bukkit.getConsoleSender().sendMessage(String.format("§a%s插件v%s已停止！§e开发者：§b§l%s",
                plugin.getDescription().getPrefix(), plugin.getDescription().getVersion(), plugin.getDescription().getAuthors().get(0)));
    }

    private static String humpToLine2(String str){
        Pattern humpPattern = Pattern.compile("[A-Z]");
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()){
            matcher.appendReplacement(sb, "-"+matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static void printConfigFormat(Object object) {
        for (Field field : object.getClass().getDeclaredFields()) {
            String key = humpToLine2(field.getName());
            switch (field.getType().getName().hashCode()) {
                case 104431://int
                    System.out.println(humpToLine2(field.getName()) + ": 0");
                    break;
                case 3327612://long
                    System.out.println(humpToLine2(field.getName()) + ": 0");
                    break;
                case 64711720://boolean
                    System.out.println(humpToLine2(field.getName()) + ": false");
                    break;
                case 1195259493://string
                    System.out.println(humpToLine2(field.getName()) + ": ''");
                    break;
                case 65821278://list
                    System.out.println(humpToLine2(field.getName()) + ": []");
                    break;
            }
        }
    }

    public static void loadSimpleConfig(JavaPlugin plugin, Object object) {
        FileConfiguration cfg = loadCheckConfig(plugin);
        for (Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            String key = humpToLine2(field.getName());
            try {
                switch (field.getType().getName().hashCode()) {
                    case 104431://int
                        field.setInt(object, cfg.getInt(key));
                        break;
                    case 3327612://long
                        field.setLong(object, cfg.getLong(key));
                        break;
                    case 64711720://boolean
                        field.setBoolean(object, cfg.getBoolean(key));
                        break;
                    case 1195259493://string
                        field.set(object, cfg.getString(key));
                        break;
                    case 65821278://list
                        field.set(object, cfg.getStringList(key));
                        break;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
