package com.nautilusmc.nautilussentry.commands;

import com.nautilusmc.nautilussentry.utils.DiscordWebhookUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Commands implements CommandExecutor {
    private final JavaPlugin plugin;

    public Commands(JavaPlugin plugin, YamlConfiguration messagesConfig) {
        this.plugin = plugin;
        this.messagesConfig = messagesConfig;
    }

    // Load messages.yml
    private final YamlConfiguration messagesConfig;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ns")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be executed by a player.");
                return true;
            }

            Player player = (Player) sender;

            if (args.length >= 3 && args[0].equalsIgnoreCase("add")) {
                if (!player.hasPermission("nautilussentry.add")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messagesConfig.getString("no-permission"))));
                    return true;
                }

                String playerName = args[1];
                String comment = String.join(" ", args).substring(args[0].length() + args[1].length() + 2);

                // Retrieve the player corresponding to the indicated nickname (ignoring the case)
                OfflinePlayer targetPlayer = getPlayerByName(playerName);
                if (targetPlayer == null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messagesConfig.getString("player-not-found")).replace("%player%", playerName)));
                    return true;
                }

                // Retrieve the UUID of the corresponding player
                UUID targetUUID = targetPlayer.getUniqueId();

                // Create or load the data.yml file
                File dataFile = new File(plugin.getDataFolder(), "data.yml");
                YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);

                // Get the list of existing comments for the player
                List<String> comments = dataConfig.getStringList("comments." + targetUUID.toString() + ".comments");

                // Add the new comment to the list
                comments.add(comment);

                // Update comments in data.yml file
                dataConfig.set("comments." + targetUUID.toString() + ".uuid", targetUUID.toString());
                dataConfig.set("comments." + targetUUID.toString() + ".pseudo", targetPlayer.getName());
                dataConfig.set("comments." + targetUUID.toString() + ".comments", comments);

                try {
                    dataConfig.save(dataFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messagesConfig.getString("error-on-save"))));
                    return true;
                }

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messagesConfig.getString("add-comment")).replace("%player%", playerName)));

                // Charger le fichier discordwebhookurl.yml
                File webhookFile = new File(plugin.getDataFolder(), "discordwebhookurl.yml");
                YamlConfiguration webhookConfig = YamlConfiguration.loadConfiguration(webhookFile);

                // Vérifier la valeur de l'option enable-webhook dans discordwebhookurl.yml
                if (webhookConfig.getBoolean("enable-webhook")) {
                    // Exécuter la méthode DiscordWebhookUtil.sendWebhook seulement si enable-webhook est true
                    DiscordWebhookUtil.sendWebhook(playerName, comment);
                }

                return true;
            } else if (args.length == 2 && args[0].equalsIgnoreCase("view")) {
                if (!player.hasPermission("nautilussentry.view")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messagesConfig.getString("no-permission"))));
                    return true;
                }

                String playerName = args[1];

                // Get the player corresponding to the indicated nickname (ignoring the case)
                OfflinePlayer targetPlayer = getPlayerByName(playerName);
                if (targetPlayer == null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messagesConfig.getString("player-not-found"))));
                    return true;
                }

                // Retrieve the UUID of the corresponding player
                UUID targetUUID = targetPlayer.getUniqueId();

                // Load the data.yml file
                File dataFile = new File(plugin.getDataFolder(), "data.yml");
                YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);

                // Check if the player has saved comments
                if (!dataConfig.isConfigurationSection("comments." + targetUUID.toString())) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messagesConfig.getString("no-comments")).replace("%player%", playerName)));
                    return true;
                }

                // Get the list of comments for the specified player
                List<String> comments = dataConfig.getStringList("comments." + targetUUID.toString() + ".comments");

                String commentListMessage = messagesConfig.getString("player-comment-list");
                assert commentListMessage != null;
                commentListMessage = commentListMessage.replace("%player%", Objects.requireNonNull(targetPlayer.getName())).replace("%uuid%", targetUUID.toString());
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', commentListMessage));

                if (comments.isEmpty()) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messagesConfig.getString("no-comments")).replace("%player%", playerName)));
                } else {
                    for (String comment : comments) {
                        sender.sendMessage("- " + comment);
                    }
                }

                return true;
            } else if (args.length == 2 && args[0].equalsIgnoreCase("clear")) {
                if (!player.hasPermission("nautilussentry.clear")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messagesConfig.getString("no-permission"))));
                    return true;
                }

                String playerName = args[1];

                // Retrieve the player corresponding to the indicated nickname (ignoring the case)
                OfflinePlayer targetPlayer = getPlayerByName(playerName);
                if (targetPlayer == null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messagesConfig.getString("player-not-found"))));
                    return true;
                }

                // Retrieve the UUID of the corresponding player
                UUID targetUUID = targetPlayer.getUniqueId();

                // Load the data.yml file
                File dataFile = new File(plugin.getDataFolder(), "data.yml");
                YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);

                // Delete the player's comments
                dataConfig.set("comments." + targetUUID.toString(), null);

                try {
                    dataConfig.save(dataFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messagesConfig.getString("error-on-delete"))));
                    return true;
                }

                String deleteConfirmationMessage = messagesConfig.getString("clear-comments");
                assert deleteConfirmationMessage != null;
                deleteConfirmationMessage = deleteConfirmationMessage.replace("%player%", Objects.requireNonNull(targetPlayer.getName()));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', deleteConfirmationMessage));
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
                if (!player.hasPermission("nautilussentry.list")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messagesConfig.getString("no-permission"))));
                    return true;
                }

                // Load the data.yml file
                File dataFile = new File(plugin.getDataFolder(), "data.yml");
                YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);

                // Get the list of all players with comments
                List<String> playerList = new ArrayList<>();
                if (dataConfig.isConfigurationSection("comments")) {
                    for (String key : Objects.requireNonNull(dataConfig.getConfigurationSection("comments")).getKeys(false)) {
                        String pseudo = dataConfig.getString("comments." + key + ".pseudo");
                        String uuid = dataConfig.getString("comments." + key + ".uuid");
                        playerList.add(ChatColor.GRAY + "- " + pseudo + ChatColor.DARK_GRAY + " (" + uuid + ")");
                    }
                }

                if (playerList.isEmpty()) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messagesConfig.getString("list-is-empty"))));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messagesConfig.getString("commented-players-list"))));
                    for (String playerInfo : playerList) {
                        sender.sendMessage(playerInfo);
                    }
                }

                return true;
            }
        }

        return false;
    }

    // Method to get a player by their name (ignoring case)
    private OfflinePlayer getPlayerByName(String name) {
        OfflinePlayer onlinePlayer = Bukkit.getPlayerExact(name);
        if (onlinePlayer != null) {
            return onlinePlayer;
        }

        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(name)) {
                return offlinePlayer;
            }
        }

        return null;
    }
}
