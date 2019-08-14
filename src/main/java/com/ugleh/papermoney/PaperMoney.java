package com.ugleh.papermoney;

import com.ugleh.papermoney.command.CommandDeposit;
import com.ugleh.papermoney.command.CommandPaperMoney;
import com.ugleh.papermoney.command.CommandWithdraw;
import com.ugleh.papermoney.config.ConfigGeneral;
import com.ugleh.papermoney.tapcompleter.TapCompleterPaperMoney;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;

public class PaperMoney extends JavaPlugin {
    private static Economy econ = null;
    private static PaperMoney instance = null;
    private static ItemStack bankNote;
    private static ConfigGeneral configLanguage = null;

    @Override
    public void onEnable() {
        Metrics metrics = new Metrics(this);
        configLanguage = new ConfigGeneral(this);
        if(!setupEconomy()) {
            this.getLogger().log(Level.WARNING, getLanguage().langStringColor("console.novault"));
            getServer().getPluginManager().disablePlugin(this);
        }
        instance = this;
        createBankNote();
        this.getCommand("papermoney").setExecutor(new CommandPaperMoney());
        this.getCommand("papermoney").setTabCompleter(new TapCompleterPaperMoney());
        this.getCommand("deposit").setExecutor(new CommandDeposit());
        this.getCommand("withdraw").setExecutor(new CommandWithdraw());
    }

    @Override
    public void onDisable() {
        this.getLogger().log(Level.INFO, getLanguage().langStringColor("console.disabled"));
    }

    private void createBankNote() {
        bankNote = new ItemStack(Material.PAPER);
        bankNote.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
        ItemMeta bankNoteMeta = bankNote.getItemMeta();
        bankNoteMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        bankNoteMeta.setLore(new ArrayList<>(Collections.singletonList(getLanguage().langStringColor("item.lore"))));
        bankNote.setItemMeta(bankNoteMeta);
    }


    private boolean setupEconomy() {
        if(getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rps = getServer().getServicesManager().getRegistration(Economy.class);
        if(rps == null) return false;
        econ = rps.getProvider();
        return econ != null;
    }

    public static PaperMoney getInstance() {
        return instance;
    }
    public static Economy getEconomy() {
        return econ;
    }
    public static ItemStack getBankNote() {
        return bankNote;
    }
    public static ConfigGeneral getLanguage() {
        return configLanguage;
    }
}
