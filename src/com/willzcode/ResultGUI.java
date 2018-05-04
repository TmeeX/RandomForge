package com.willzcode;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;

/**
 * Created by willz on 2018/4/30.
 */
public class ResultGUI implements Listener {
    public static final String TITLE = "§a§l请拿走你的物品";
    private Inventory in;

    public ResultGUI(ItemStack item) {
        this.in = Bukkit.createInventory((InventoryHolder)null, 9, TITLE);
        this.in.setItem(4, item);
    }

    public ResultGUI() {
    }

    public void open(Player p) {
        p.openInventory(this.in);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Inventory i = e.getInventory();
        if(TITLE.equals(i.getTitle())) {
            for(int j = 0; j < e.getInventory().getSize(); ++j) {
                ItemStack itemStack = e.getInventory().getItem(j);
                if(itemStack != null) {
                    e.getPlayer().getInventory().addItem(itemStack);
                }
            }
        }

    }
}