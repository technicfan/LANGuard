package net.ovilli.languard;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LANGuard implements ModInitializer {

    public static final Set<String> WHITELIST = Collections.synchronizedSet(new HashSet<>());
    public static Path CONFIG_DIR;
    private static String CLIENT_NAME;
    private static boolean CLIENT_NAME_SET = false;

    @Override
    public void onInitialize() {
        CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
        loadWhitelist();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            checkPlayer(handler.getPlayer(), server);
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            checkPlayer(newPlayer, newPlayer.getServer());
        });
    }

    public static void setClientName(String name) {
        if (CLIENT_NAME_SET) {
            throw new IllegalStateException(
                    "CLIENT_NAME can only be set once (by the client init function)!"
            );
        }
        CLIENT_NAME = name;
        CLIENT_NAME_SET = true;

        // Add client to whitelist if not present
        if (!WHITELIST.contains(name)) {
            WHITELIST.add(name);
            saveWhitelistToFile(new ArrayList<>(WHITELIST));
            System.out.println("[Whitelist] Auto-added local player '" + name + "' to whitelist.");
        }
    }

    private void loadWhitelist() {
        try {
            File whitelistFile = CONFIG_DIR.resolve("whitelist.json").toFile();

            if (!whitelistFile.exists()) {
                System.out.println("[Whitelist] whitelist.json not found!");
                return;
            }

            try (FileReader reader = new FileReader(whitelistFile)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                WHITELIST.clear();

                if (json.has("whitelist")) {
                    for (var elem : json.getAsJsonArray("whitelist")) {
                        WHITELIST.add(elem.getAsString());
                    }
                }

                System.out.println("[Whitelist] Loaded usernames: " + WHITELIST);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveWhitelistToFile(List<String> whitelist) {
        File whitelistFile = CONFIG_DIR.resolve("whitelist.json").toFile();

        try (FileWriter writer = new FileWriter(whitelistFile)) {
            StringBuilder json = new StringBuilder();
            json.append("{\n  \"whitelist\": [\n");
            for (int i = 0; i < whitelist.size(); i++) {
                json.append("    \"").append(whitelist.get(i)).append("\"");
                if (i < whitelist.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("  ]\n}");

            writer.write(json.toString());
            System.out.println("[Whitelist] Saved whitelist.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkPlayer(ServerPlayerEntity player, MinecraftServer server) {
        String username = player.getGameProfile().getName();
        UUID reportedUuid = player.getGameProfile().getId();

        System.out.println("[Whitelist] Player joined: " + username);
        System.out.println("[Whitelist] Reported UUID: " + reportedUuid);

        new Thread(() -> {
            try {
                URL url = new URL("https://playerdb.co/api/player/minecraft/" + username);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "LANGuard/1.2");

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();

                if (!json.get("success").getAsBoolean()) {
                    System.out.println("[Whitelist] PlayerDB API failed for " + username);
                    server.execute(() ->
                            player.networkHandler.disconnect(Text.of("Could not verify your identity.")));
                    return;
                }

                String uuidStr = json.getAsJsonObject("data").getAsJsonObject("player").get("id").getAsString();
                UUID officialUuid = UUID.fromString(uuidStr);

                if (!officialUuid.equals(reportedUuid)) {
                    System.out.println("[Whitelist] UUID mismatch: " + officialUuid + " != " + reportedUuid);
                    server.execute(() ->
                            player.networkHandler.disconnect(Text.of("Identity verification failed.")));
                } else {
                    System.out.println("[Whitelist] Verified player: " + username);

                    if (!WHITELIST.contains(username)) {
                        WHITELIST.add(username);
                        saveWhitelistToFile(new ArrayList<>(WHITELIST));
                        System.out.println("[Whitelist] Auto-added verified player: " + username);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                server.execute(() ->
                        player.networkHandler.disconnect(Text.of("Whitelist verification error.")));
            }
        }).start();
    }
}
