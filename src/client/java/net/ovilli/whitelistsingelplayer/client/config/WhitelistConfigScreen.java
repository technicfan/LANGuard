package net.ovilli.whitelistsingelplayer.client.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.ovilli.whitelistsingelplayer.Whitelistsingelplayer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static me.shedaniel.clothconfig2.api.ConfigBuilder.*;

public class WhitelistConfigScreen {

    public static Screen getScreen(Screen parent) {
        ConfigBuilder builder = create()
                .setParentScreen(parent)
                .setTitle(Text.of("Whitelist Config"))
                .setSavingRunnable(() -> {
                    // Optionally do something after save
                });

        ConfigCategory category = builder.getOrCreateCategory(Text.of("Whitelist Settings"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // Copy whitelist into a list that can be modified in the GUI
        List<String> whitelistList = new ArrayList<>(Whitelistsingelplayer.WHITELIST);

        // StrList with add/remove UI
        category.addEntry(entryBuilder.startStrList(Text.of("Whitelisted Players"), whitelistList)
                .setTooltip(Text.of("Add or remove players allowed in singleplayer."))
                .setExpanded(true)
                .setDefaultValue(new ArrayList<>()) // default: empty list
                .setSaveConsumer(newList -> {
                    Whitelistsingelplayer.WHITELIST.clear();
                    Whitelistsingelplayer.WHITELIST.addAll(newList);
                    saveWhitelistToFile(newList);
                })
                .build());

        return builder.build();
    }

    private static void saveWhitelistToFile(List<String> whitelist) {
        try {
            Path configDir = Whitelistsingelplayer.CONFIG_DIR;
            File whitelistFile = configDir.resolve("whitelist.json").toFile();

            StringBuilder json = new StringBuilder();
            json.append("{\n  \"whitelist\": [\n");
            for (int i = 0; i < whitelist.size(); i++) {
                json.append("    \"").append(whitelist.get(i)).append("\"");
                if (i < whitelist.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("  ]\n}");

            try (FileWriter writer = new FileWriter(whitelistFile)) {
                writer.write(json.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
