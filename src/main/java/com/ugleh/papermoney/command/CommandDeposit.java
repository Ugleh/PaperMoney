package com.ugleh.papermoney.command;

import com.ugleh.papermoney.PaperMoney;
import com.ugleh.papermoney.util.XSound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

public class CommandDeposit implements CommandExecutor {

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(!(commandSender instanceof Player)) return notPlayer(commandSender);
        if(!commandSender.hasPermission("papermoney.deposit")) return noPermission(commandSender);

        Player player = ((Player) commandSender);
        ItemStack itemInMainHand;
        try {
            itemInMainHand = (ItemStack) player.getInventory().getClass().getMethod("getItemInHand").invoke(player.getInventory());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            itemInMainHand = player.getInventory().getItemInMainHand();
        }

        NBTItem nbtItem = new NBTItem(itemInMainHand);
        if(!nbtItem.hasNBTData()) return wrongItem(player);
        if(!nbtItem.hasKey("PaperMoneyValue")) return wrongItem(player);

        double depositAmount = nbtItem.getDouble("PaperMoneyValue");
        Economy economy = PaperMoney.getEconomy();
        economy.depositPlayer(player, player.getWorld().getName(), depositAmount);
        PaperMoney.getLanguage().messagePlayer(player, Level.INFO, "message.info.deposited");
        PaperMoney.getLanguage().messagePlayer(player, Level.INFO, "message.info.balance", economy.format(economy.getBalance(player, player.getWorld().getName())) );
        player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0f, 1.0f);
        if(itemInMainHand.getAmount() == 1)
            player.getInventory().remove(itemInMainHand);
        else
            itemInMainHand.setAmount(itemInMainHand.getAmount()-1);

        return true;
    }

    private boolean wrongItem(Player player) {
        PaperMoney.getLanguage().messagePlayer(player, Level.WARNING, "message.warning.wrongitem");
        player.playSound(player.getLocation(), XSound.BLOCK_ANVIL_LAND.parseSound(), 0.2f, 1f);
        return true;
    }

    private boolean notPlayer(CommandSender commandSender) {
        PaperMoney.getLanguage().messageSender(commandSender, Level.WARNING, "message.warning.noconsole");
        return true;
    }
    private boolean noPermission(CommandSender commandSender) {
        PaperMoney.getLanguage().messageSender(commandSender, Level.WARNING, "message.warning.nopermission");
        return true;
    }
}
