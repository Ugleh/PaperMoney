package com.ugleh.papermoney.command;

import com.ugleh.papermoney.PaperMoney;
import com.ugleh.papermoney.util.XSound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class CommandWithdraw implements CommandExecutor {

    private HashMap<UUID, Long> withdrawTimerList = new HashMap<>();


    public CommandWithdraw() {
        Bukkit.getScheduler().runTaskTimer(PaperMoney.getInstance(), () -> {
            long rightNow = Instant.now().getEpochSecond();
            int withdrawTimer = PaperMoney.getLanguage().withdrawTimer;

            for (Map.Entry<UUID, Long> playerEntry : withdrawTimerList.entrySet()) {
                if(playerEntry.getValue() < rightNow-withdrawTimer)
                    withdrawTimerList.remove(playerEntry.getKey());
            }
        }, 0L, 3600 * 20L); // 3600 is 1 hour in seconds
    }
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(!(commandSender instanceof Player)) return notPlayer(commandSender);
        if(!commandSender.hasPermission("papermoney.withdraw")) return noPermission(commandSender);
        Player player = ((Player) commandSender);
        if(args.length != 1) return incorrectArgAmount(player);
        if(withdrawTimerList.containsKey(player.getUniqueId())) {
            long savedEpochSecond = withdrawTimerList.get(player.getUniqueId());
            long difference = (Instant.now().getEpochSecond() - savedEpochSecond);
            if(difference < PaperMoney.getLanguage().withdrawTimer) return tooFast(player, PaperMoney.getLanguage().withdrawTimer-difference);
            withdrawTimerList.put(player.getUniqueId(), Instant.now().getEpochSecond());
        }else {
            withdrawTimerList.put(player.getUniqueId(), Instant.now().getEpochSecond());
        }
        double withdrawRequest;

        try {
            withdrawRequest  = Double.parseDouble(args[0]);
        }catch(NumberFormatException e) {
            return incorrectArgAmount(player);
        }

        if(withdrawRequest == 0d) return amountCannotBeZero(player);
        if(withdrawRequest < PaperMoney.getLanguage().withdrawMinimum) return amountTooSmall(player);
        Economy economy = PaperMoney.getEconomy();
        double playerBalance = economy.getBalance(player, player.getWorld().getName());
        String withdrawRequestFormatted = economy.format(withdrawRequest);
        String playerBalanceFormatted = economy.format(playerBalance);
        if(playerBalance < withdrawRequest) return notEnoughMoney(player, playerBalanceFormatted);
        economy.withdrawPlayer(player, player.getWorld().getName(), withdrawRequest);
        ItemStack customBankNote = createCustomBankNote(withdrawRequest, withdrawRequestFormatted);
        if(player.getInventory().firstEmpty() == -1) {
            PaperMoney.getLanguage().messagePlayer(player, Level.INFO, "message.info.withdrew", withdrawRequestFormatted);
            PaperMoney.getLanguage().messagePlayer(player, Level.INFO, "message.info.inventoryfull");
            PaperMoney.getLanguage().messagePlayer(player, Level.INFO, "message.info.balance", economy.format(economy.getBalance(player, player.getWorld().getName())) );
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0f, 1.0f);
            player.getWorld().dropItem(player.getLocation(), customBankNote);
        }else {
            PaperMoney.getLanguage().messagePlayer(player, Level.INFO, "message.info.withdrew", withdrawRequestFormatted);
            PaperMoney.getLanguage().messagePlayer(player, Level.INFO, "message.info.balance", economy.format(economy.getBalance(player, player.getWorld().getName())) );

            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0f, 1.0f);
            player.getInventory().addItem(customBankNote);
        }
        return true;
    }

    private boolean tooFast(Player player, long timeLeft) {
        PaperMoney.getLanguage().messagePlayer(player, Level.WARNING, "message.warning.toofast", String.valueOf(timeLeft));
        player.playSound(player.getLocation(), XSound.BLOCK_ANVIL_LAND.parseSound(), 0.2f, 1f);
        return true;

    }

    private boolean noPermission(CommandSender commandSender) {
        PaperMoney.getLanguage().messageSender(commandSender, Level.WARNING, "message.warning.nopermission");
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

    private boolean notEnoughMoney(Player player, String balance) {
        PaperMoney.getLanguage().messagePlayer(player, Level.WARNING, "message.warning.nofunds");
        PaperMoney.getLanguage().messagePlayer(player, Level.WARNING, "message.info.balance", balance);
        player.playSound(player.getLocation(), XSound.BLOCK_ANVIL_LAND.parseSound(), 0.2f, 1f);
        return true;

    }

    private boolean incorrectArgAmount(Player player) {
        PaperMoney.getLanguage().messagePlayer(player, Level.WARNING, "message.warning.wrongcommandusage");
        PaperMoney.getLanguage().messagePlayer(player, Level.WARNING, "message.warning.withdrawusage");
        player.playSound(player.getLocation(), XSound.BLOCK_ANVIL_LAND.parseSound(), 0.2f, 1f);
        return true;
    }

    private boolean notPlayer(CommandSender commandSender) {
        PaperMoney.getLanguage().messageSender(commandSender, Level.WARNING, "message.warning.noconsole");
        return true;
    }

    private boolean amountCannotBeZero(Player player) {
        PaperMoney.getLanguage().messagePlayer(player, Level.WARNING, "message.warning.notzero");
        player.playSound(player.getLocation(), XSound.BLOCK_ANVIL_LAND.parseSound(), 0.2f, 1f);
        return true;
    }

    private boolean amountTooSmall(Player player) {
        PaperMoney.getLanguage().messagePlayer(player, Level.WARNING, "message.warning.toolittle", PaperMoney.getEconomy().format(PaperMoney.getLanguage().withdrawMinimum));
        player.playSound(player.getLocation(), XSound.BLOCK_ANVIL_LAND.parseSound(), 0.2f, 1f);
        return true;
    }

}
