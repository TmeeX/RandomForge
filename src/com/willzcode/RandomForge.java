package com.willzcode;

import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Created by willz on 2018/4/19.
 * Forge random things
 */
@SuppressWarnings("deprecation")
public class RandomForge extends JavaPlugin implements Listener {

    private Map<String, ForgeItem> forgeItems;
    private boolean isGlobalUnbreakable = false;
    private String colorList;
    private boolean isEnableResultGUI = false;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("randomforge.admin")) {
            loadcfg();
            sender.sendMessage("配置重载！");
            return true;
        }

        if(!sender.hasPermission("randomforge.use"))
            return true;

        if (args.length >= 2) {
            String name = args[0];
            String itemKey = args[1];
            Player p = getServer().getPlayer(name);
            ForgeItem item = forgeItems.get(itemKey);
            if (p != null && item != null) {
                ItemStack stack = new ItemStack(item.id, 1, item.data);
                ItemMeta im = stack.getItemMeta();

                if(item.unbreakable)
                    try {
                        im.setUnbreakable(true);
                    } catch (Exception ignored) {

                    }

                if(item.hideAttribute)
                    try {
                        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    } catch (Exception ignored) {

                    }

                Map<String, String> replacement = makeLevelReplacement(item, name);

                String display = item.display;
                for (String key : replacement.keySet()) {
                    display = display.replace(key, replacement.get(key));
                }
                im.setDisplayName(color(display));

                im.setLore(replaceLores(item.lores, replacement));


                stack.setItemMeta(im);
                if (isEnableResultGUI) {
                    ResultGUI rg =  new ResultGUI(stack);
                    rg.open(p);
                } else {
                    p.getInventory().addItem(stack);
                }
            }
        }

        return true;
    }

    private Map<String, String> makeLevelReplacement(ForgeItem forgeItem, String player) {
        Map<String, String> replacement = new HashMap<>();
        Map<Integer, Integer> weightMap = new HashMap<>();
        int bound = 0;

        for (int i = 0; i < forgeItem.levels.size(); i++) {
            int weight = forgeItem.levels.get(i).weight;
            weightMap.put(i, weight);
            bound += weight;
        }

        //Rolling level
        Random ran = new Random();
        ran.setSeed(System.currentTimeMillis());
        ForgeLevel rolledLevel = null;
        int roll = ran.nextInt(bound);
        for (int i = 0; i < forgeItem.levels.size(); i++) {
            if (weightMap.get(i) > roll) {
                //roll get
                rolledLevel = forgeItem.levels.get(i);
                break;
            } else {
                roll -= weightMap.get(i);
            }
        }

        int strength = ran.nextInt(100);

        //Randomize Values
        if (rolledLevel != null) {
            for (ForgeField field : rolledLevel.fields) {
                if (field.fieldType == ForgeField.FieldType.FIXED) {
                    replacement.put("<"+field.fieldName+">", field.value);
                } else if (field.fieldType == ForgeField.FieldType.RANDOM) {
                    replacement.put("<" + field.fieldName + ">", String.valueOf(ran.nextInt(field.value2 - field.value1) + field.value1));
                } else if (field.fieldType == ForgeField.FieldType.FLOAT) {
                    double v = ran.nextDouble() * (field.bound - field.start) + field.start;
                    String fv = String.format("%."+field.decimal+"f", v);
                    replacement.put("<" + field.fieldName + ">", fv);
                } else if (field.fieldType == ForgeField.FieldType.LORES) {
                    List<String> lores = field.lores;
                    replacement.put("<" + field.fieldName + ">", lores.get(ran.nextInt(lores.size())));
                } else {
                    replacement.put("<" + field.fieldName + ">", String.valueOf((field.base + field.add * strength / 100)));
                }
            }
        }

        replacement.put("<player>", player);

        char code = colorList.charAt(strength / colorList.length());
        int t = strength / 5;
        String strengthstr = "&" + code;
        for(int i = 0; i < 20; i++) {
            strengthstr += '|';
            if (i == t) {
                strengthstr += "&8";
            }
        }
        replacement.put("<strength>", strengthstr);

        return replacement;
    }

    private List<String> replaceLores(List<String> lores, Map<String, String> replacement) {
        List<String> newlores = new ArrayList<>();
        for (String lore : lores) {
            for (String key : replacement.keySet()) {
                lore = lore.replace(key, replacement.get(key));
            }
            newlores.add(color(lore));
        }
        return newlores;
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    @Override
    public void onDisable() {
        PluginHelper.doDisable(this);
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new ResultGUI(), this);

//        getLogger().info(Bukkit.getServer().getBukkitVersion());
//        getLogger().info(Bukkit.getServer().getVersion());

        PluginHelper.doEnable(this);
        loadcfg();
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        if(isGlobalUnbreakable)
            event.setCancelled(true);
    }

    private void loadcfg() {
        saveDefaultConfig();
        reloadConfig();
        FileConfiguration cfg = getConfig();

        forgeItems = new HashMap<>();
        String ITEM_SECTION_PATH = "items";
        for (String key : cfg.getConfigurationSection(ITEM_SECTION_PATH).getKeys(false)) {
            ForgeItem item = new ForgeItem();
            String ITEM_FIELD_FORMAT = ITEM_SECTION_PATH + ".%s.%s";
            item.id = cfg.getInt(String.format(ITEM_FIELD_FORMAT, key, "id"));
            item.data = (short) cfg.getInt(String.format(ITEM_FIELD_FORMAT, key, "data"));
            item.display = cfg.getString(String.format(ITEM_FIELD_FORMAT, key, "display"));
            item.unbreakable = cfg.getBoolean(String.format(ITEM_FIELD_FORMAT, key, "unbreakable"));
            item.hideAttribute = cfg.getBoolean(String.format(ITEM_FIELD_FORMAT, key, "hide-attribute"));
            item.lores = cfg.getStringList(String.format(ITEM_FIELD_FORMAT, key, "lores"));
            item.levels = new LinkedList<>();
            final String LEVEL_PATH = String.format(ITEM_FIELD_FORMAT, key, "levels");
            for (String level : cfg.getConfigurationSection(LEVEL_PATH).getKeys(false)) {
                String thislevelpath = LEVEL_PATH + "." + level;
                ForgeLevel forgeLevel = new ForgeLevel();
                forgeLevel.weight = cfg.getInt(thislevelpath + ".weight");
                if (forgeLevel.weight <= 0) {
                    getLogger().warning("配置读取异常：权重必须为正数");
                    getLogger().warning("配置定位：" + thislevelpath);
                    return;
                }
                forgeLevel.fields = new LinkedList<>();
                for (String field : cfg.getConfigurationSection(thislevelpath).getKeys(false)) {
                    if (field.equalsIgnoreCase("weight")) {
                        continue;
                    }
                    String thisfieldpath = thislevelpath + "." + field;
                    ForgeField forgeField = new ForgeField();
                    forgeField.fieldName = field;
                    String type = cfg.getString(thisfieldpath + ".type");
                    if (type.equalsIgnoreCase("fixed")) {
                        forgeField.fieldType = ForgeField.FieldType.FIXED;
                        forgeField.value = cfg.getString(thisfieldpath + ".value");
                    } else if (type.equalsIgnoreCase("random")) {
                        forgeField.fieldType = ForgeField.FieldType.RANDOM;
                        forgeField.value1 = cfg.getInt(thisfieldpath + ".value1");
                        forgeField.value2 = cfg.getInt(thisfieldpath + ".value2");
                        if (forgeField.value2 <= forgeField.value1) {
                            getLogger().warning("配置读取异常：value2必须大于value1");
                            getLogger().warning("配置定位：" + thisfieldpath);
                            return;
                        }
                    } else if (type.equalsIgnoreCase("lores")) {
                        forgeField.fieldType = ForgeField.FieldType.LORES;
                        forgeField.lores = cfg.getStringList(thisfieldpath + ".value");
                    } else if (type.equalsIgnoreCase("float")) {
                        forgeField.fieldType = ForgeField.FieldType.FLOAT;
                        forgeField.decimal = cfg.getInt(thisfieldpath + ".decimal");
                        if (forgeField.decimal < 0) {
                            getLogger().warning("配置读取异常：decimal不能为负数");
                            getLogger().warning("配置定位：" + thisfieldpath);
                            return;
                        }
                        forgeField.start = cfg.getDouble(thisfieldpath + ".start");
                        forgeField.bound = cfg.getDouble(thisfieldpath + ".bound");
                        if (forgeField.bound <= forgeField.start) {
                            getLogger().warning("配置读取异常：bound必须大于start");
                            getLogger().warning("配置定位：" + thisfieldpath);
                            return;
                        }
                    } else {
                        forgeField.fieldType = ForgeField.FieldType.STRENGTH;
                        forgeField.base = cfg.getInt(thisfieldpath + ".base");
                        forgeField.add = cfg.getInt(thisfieldpath + ".add");
                    }

                    forgeLevel.fields.add(forgeField);
                }
                item.levels.add(forgeLevel);
            }


            forgeItems.put(key, item);
        }

        isGlobalUnbreakable = cfg.getBoolean("options.global-unbreakable");
        colorList = cfg.getString("options.strength-color");
        isEnableResultGUI = cfg.getBoolean("options.result-gui");
    }
}
