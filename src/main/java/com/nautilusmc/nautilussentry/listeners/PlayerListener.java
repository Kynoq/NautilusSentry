package com.nautilusmc.nautilussentry.listeners;

import com.nautilusmc.nautilussentry.NautilusSentry;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class PlayerListener implements Listener {
    private final Plugin plugin;
    private final YamlConfiguration messagesConfig;

    public PlayerListener(NautilusSentry plugin) {
        this.plugin = plugin;
        this.messagesConfig = plugin.getMessagesConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Check if the player has a saved comment
        String uuid = event.getPlayer().getUniqueId().toString();
        if (hasComment(uuid)) {
            // Send message to players with nautilussentry.notify permission
            String message = messagesConfig.getString("alert-on-connect");
            if (message != null && !message.isEmpty()) {
                Player connectingPlayer = event.getPlayer();
                String formattedMessage = message.replace("%player%", connectingPlayer.getName());
                formattedMessage = ChatColor.translateAlternateColorCodes('&', formattedMessage);
                plugin.getServer().broadcast(formattedMessage, "nautilussentry.notify");
            }
        }
    }

    // Check if the player has a comment in the data.yml file
    private boolean hasComment(String uuid) {
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        return dataConfig.isConfigurationSection("comments." + uuid);
    }
}
