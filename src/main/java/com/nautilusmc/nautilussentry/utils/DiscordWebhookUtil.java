package com.nautilusmc.nautilussentry.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DiscordWebhookUtil {
    private static final String CONFIG_FILE_NAME = "discordwebhookurl.yml";
    private static final String WEBHOOK_URL_PATH = "webhook-url";
    private static final String WEBHOOK_ENABLED = "enable-webhook";
    private static String WEBHOOK_URL;

    public static void initialize(Plugin plugin) {
        File configFile = new File(plugin.getDataFolder(), CONFIG_FILE_NAME);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        if (!config.contains(WEBHOOK_URL_PATH)) {
            config.set(WEBHOOK_URL_PATH, "https://discord.com/api/webhooks/your-webhook-url");
            config.set(WEBHOOK_ENABLED, false);
            try {
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        WEBHOOK_URL = config.getString(WEBHOOK_URL_PATH);
    }

    public static void sendWebhook(String pseudo, String commentaire) {
        if (pseudo == null || commentaire == null) {
            // Gérer l'erreur
            return;
        }

        try {
            // Encodage des valeurs en JSON
            Gson gson = new Gson();
            String pseudoJson = gson.toJson(pseudo);
            String commentaireJson = gson.toJson(commentaire);

            // Construction de l'objet JSON final
            JsonObject json = new JsonObject();
            json.addProperty("pseudo", pseudoJson);
            json.addProperty("commentaire", commentaireJson);

            // Conversion de l'objet JSON en chaîne JSON
            String jsonPayload = "{\"content\": \"" + "Pseudo : " + pseudo + "\\nRemarque : " + commentaire + "\"}";

            // Envoi du webhook avec le payload JSON
            URL url = new URL(WEBHOOK_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            try (OutputStream outputStream = conn.getOutputStream()) {
                outputStream.write(jsonPayload.getBytes());
                outputStream.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != 204) {
                System.out.println("[NautilusSentry] An error occurred while sending the webhook. Response code: " + responseCode);
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
