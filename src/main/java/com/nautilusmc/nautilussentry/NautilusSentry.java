package com.nautilusmc.nautilussentry;

import com.nautilusmc.nautilussentry.commands.Commands;
import com.nautilusmc.nautilussentry.listeners.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public final class NautilusSentry extends JavaPlugin {

    private YamlConfiguration messagesConfig;
    public YamlConfiguration getMessagesConfig() {
        return messagesConfig;
    }
    @Override
    public void onEnable() {

        // Load messages.yml
        loadMessages();

        // Plugin started
        getLogger().info("NautilusSentry a été activé !");

        // Commands register
        Objects.requireNonNull(getCommand("ns")).setExecutor(new Commands(this, messagesConfig));

        // Listeners register
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown
        getLogger().info("NautilusSentry a été désactivé !");
    }

    private void loadMessages() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
}
