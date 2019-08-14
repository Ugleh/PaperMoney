package com.ugleh.papermoney.command;

import com.ugleh.papermoney.PaperMoney;
import com.ugleh.papermoney.util.XSound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.logging.Level;

public class CommandPaperMoney implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(!commandSender.hasPermission("papermoney.admin")) return noPermission(commandSender);
        if(args.length == 0) return incorrectCommandUsage(commandSender);
            if(args[0].equalsIgnoreCase("give")) {
                if(args.length == 3)
                    return giveCommand(commandSender, args);
                else
                    return incorrectGiveUsage(commandSender);
            }else if(args[0].equalsIgnoreCase("reload")) {
                PaperMoney.getLanguage().reload();
                PaperMoney.getLanguage().messageSender(commandSender, Level.INFO, "message.info.configreloaded");

            }
                return true;
    }

    private boolean giveCommand(CommandSender commandSender, String[] args) {
        Player player = null;
        for(Player p : Bukkit.getOnlinePlayers()) {
            if(p.getName().equalsIgnoreCase(args[1])) {
                player = p;
                break;
            }
        }
        if(player == null) return playerNotFound(commandSender);
        double moneyAmount = Double.parseDouble(args[2]);
        if(moneyAmount == 0d) return amountCannotBeZero(commandSender);
        String moneyAmountFormatted = PaperMoney.getEconomy().format(moneyAmount);
        ItemStack customBankNote = createCustomBankNote(moneyAmount, moneyAmountFormatted);
        if(player.getInventory().firstEmpty() == -1) {
            PaperMoney.getLanguage().messageSender(commandSender, Level.INFO, "message.info.gave", moneyAmountFormatted, player.getName());
            PaperMoney.getLanguage().messagePlayer(player, Level.INFO, "message.info.given", moneyAmountFormatted);
            PaperMoney.getLanguage().messagePlayer(player, Level.INFO, "message.info.inventoryfull");
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0f, 1.0f);
            player.getWorld().dropItem(player.getLocation(), customBankNote);
        }else {
            PaperMoney.getLanguage().messageSender(commandSender, Level.INFO, "message.info.gave", moneyAmountFormatted, player.getName());
            PaperMoney.getLanguage().messagePlayer(player, Level.INFO, "message.info.given", moneyAmountFormatted);
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0f, 1.0f);
            player.getInventory().addItem(customBankNote);
        }
        return true;
    }

    private ItemStack createCustomBankNote(double withdrawRequest, String formattedBalance) {
        ItemStack itemStack = PaperMoney.getBankNote().clone();
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(PaperMoney.getLanguage().langStringColor("item.name", formattedBalance));
        itemStack.setItemMeta(itemMeta);

        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setDouble("PaperMoneyValue", withdrawRequest);

        return nbtItem.getItem();
    }

    private boolean amountCannotBeZero(CommandSender commandSender) {
        PaperMoney.getLanguage().messageSender(commandSender, Level.WARNING, "message.warning.notzero");
        return true;
    }

    private boolean playerNotFound(CommandSender commandSender) {
        PaperMoney.getLanguage().messageSender(commandSender, Level.WARNING, "message.warning.noplayer");
        return true;
    }

    private boolean incorrectGiveUsage(CommandSender commandSender) {
        PaperMoney.getLanguage().messageSender(commandSender, Level.WARNING, "message.warning.wrongcommandusage");
        PaperMoney.getLanguage().messageSender(commandSender, Level.WARNING, "message.warning.giveusage");
        return true;
    }

    private boolean noPermission(CommandSender commandSender) {
        PaperMoney.getLanguage().messageSender(commandSender, Level.WARNING, "message.warning.nopermission");
        return true;
    }

    private boolean incorrectCommandUsage(CommandSender commandSender) {
        PaperMoney.getLanguage().messageSender(commandSender, Level.WARNING, "message.warning.wrongcommandusage");
        PaperMoney.getLanguage().messageSender(commandSender, Level.WARNING, "message.warning.papermoneyusage");
        return true;
    }
}
