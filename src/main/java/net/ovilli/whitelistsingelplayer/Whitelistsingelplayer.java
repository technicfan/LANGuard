package net.ovilli.whitelistsingelplayer;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class Whitelistsingelplayer implements ModInitializer{

    public static final Set<String> WHITELIST = new HashSet<>();
    public static Path CONFIG_DIR;

    @Override
    public void onInitialize() {
        CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
        loadWhitelist();

        // Player join event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            checkPlayer(handler.getPlayer());
        });

        // Player respawn event
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            checkPlayer(newPlayer);
        });
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

                for (var elem : json.getAsJsonArray("whitelist")) {
                    WHITELIST.add(elem.getAsString());
                }
                System.out.println("[Whitelist] Loaded whitelist: " + WHITELIST);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkPlayer(ServerPlayerEntity player) {
        String name = player.getGameProfile().getName();
        if (!WHITELIST.contains(name)) {
            player.networkHandler.disconnect(Text.of("You are not allowed to join this server."));
        }
    }
}
