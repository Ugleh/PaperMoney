package com.ugleh.papermoney.listener;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class PaperMoneyListener implements Listener {

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent e) {
        if(!e.getInventory().contains(Material.PAPER)) return;
        for(ItemStack item : e.getInventory()) {
            if(item == null) continue;
            NBTItem nbtItem = new NBTItem(item);
            if(nbtItem.hasKey("PaperMoneyValue")) {
                e.setResult(new ItemStack(Material.AIR));
                break;
            }
        }
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent e) {
        if(!e.getInventory().contains(Material.PAPER)) return;
        for(ItemStack item : e.getInventory()) {
            if(item == null) continue;
            NBTItem nbtItem = new NBTItem(item);
            if(nbtItem.hasKey("PaperMoneyValue")) {
                e.getInventory().setResult(new ItemStack(Material.AIR));
                break;
            }
        }
    }
}
