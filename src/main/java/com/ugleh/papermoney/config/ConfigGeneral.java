package com.ugleh.papermoney.config;

import com.ugleh.papermoney.PaperMoney;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.MissingFormatArgumentException;
import java.util.logging.Level;

public class ConfigGeneral extends YamlConfiguration {
    private JavaPlugin plugin;
    private File file;
    private String defaults;
    private HashMap<String, String> languageNodes = new HashMap<>();
    public double withdrawMinimum;
    public int withdrawTimer;

    public ConfigGeneral(PaperMoney plugin) {
        this.plugin = plugin;
        this.defaults = "config.yml";
        this.file = new File(plugin.getDataFolder(), defaults);
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException exception) {
                exception.printStackTrace();
                plugin.getLogger().severe("Error while creating file " + file.getName());
            }
        }
        try {
            load(file);
            if (defaults != null) {
                InputStreamReader reader = new InputStreamReader(plugin.getResource(defaults));
                FileConfiguration defaultsConfig = YamlConfiguration.loadConfiguration(reader);
                setDefaults(defaultsConfig);
                options().copyDefaults(true);
                reader.close();
                save();
            }
            loadLanguageNodes();
            loadConfigNodes();
        } catch (IOException | InvalidConfigurationException exception) {
            exception.printStackTrace();
            plugin.getLogger().severe("Error while loading file " + file.getName());
        }
    }

    private void loadConfigNodes() {
        withdrawMinimum = this.getDouble("config.withdraw.minimum");
        withdrawTimer = this.getInt("config.withdraw.reusetimer");
    }

    private void loadLanguageNodes() {
        for (String languageID : this.getKeys(true)) {
            String langString = this.getString(languageID);
            if(!this.isConfigurationSection(languageID) && (!languageID.startsWith("config.")))
                languageNodes.put(languageID.replace("language.", ""), langString);
        }
    }

    private void save() {
        try {
            options().indent(2);
            save(file);
        } catch (IOException exception) {
            exception.printStackTrace();
            plugin.getLogger().severe("Error while saving file " + file.getName());
        }
    }


    public String langStringColor(String key, String... format) {
        String formattedString = ChatColor.translateAlternateColorCodes(languageNodes.get("color.character").charAt(0), languageNodes.get(key));
        if(format == null || format.length == 0) return formattedString;
        try {
            formattedString = String.format(formattedString, format);
        }catch(MissingFormatArgumentException e) {
            PaperMoney.getInstance().getLogger().warning("Language.yml file corrupted. Missing an %s in node '" + key + "'");
        }
        return formattedString;
    }

    public void messageSender(CommandSender sender, Level level, String key, String... format) {
        String messageColor;
        if (Level.WARNING.equals(level))
            messageColor = langStringColor("color.warning");
        else
            messageColor = langStringColor("color.info");
        if(format.length > 0)
            sender.sendMessage(langStringColor("message.prefix") + messageColor + langStringColor(key, format));
        else
            sender.sendMessage(langStringColor("message.prefix") + messageColor + langStringColor(key));
    }

    public void messagePlayer(Player player, Level level, String key, String... format) {
        String messageColor;
        if (Level.WARNING.equals(level))
            messageColor = langStringColor("color.warning");
        else
            messageColor = langStringColor("color.info");
        if(format.length > 0)
            player.sendMessage(langStringColor("message.prefix") + messageColor + langStringColor(key, format));
        else
            player.sendMessage(langStringColor("message.prefix") + messageColor + langStringColor(key));
    }
}
