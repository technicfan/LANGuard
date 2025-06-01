package net.ovilli.languard.client.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.ovilli.languard.LANGuard;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LANGuardConfigScreen {

    public static Screen getScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.of("Whitelist Config"))
                .setSavingRunnable(() -> {
                    // Optional save logic hook
                });

        ConfigCategory category = builder.getOrCreateCategory(Text.of("Whitelist Settings"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // Copy current whitelist into a list
        List<String> whitelistList = new ArrayList<>(LANGuard.WHITELIST);

        category.addEntry(entryBuilder.startStrList(Text.of("Whitelisted Players"), whitelistList)
                .setTooltip(Text.of("Add or remove players allowed in singleplayer."))
                .setExpanded(true)
                .setDefaultValue(new ArrayList<>())
                .setSaveConsumer(newList -> {
                    // Update whitelist memory
                    LANGuard.WHITELIST.clear();
                    LANGuard.WHITELIST.addAll(newList);

                    // Save to disk
                    saveWhitelistToFile(newList);
                })
                .build());

        return builder.build();
    }

    private static void saveWhitelistToFile(List<String> whitelist) {
        try {
            Path configDir = LANGuard.CONFIG_DIR;
            File whitelistFile = configDir.resolve("whitelist.json").toFile();

            if (!whitelistFile.getParentFile().exists()) {
                whitelistFile.getParentFile().mkdirs();
            }

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
                System.out.println("[Whitelist GUI] Saved to whitelist.json");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
