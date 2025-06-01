package net.ovilli.whitelistsingelplayer.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.ovilli.whitelistsingelplayer.Whitelistsingelplayer;

public class WhitelistsingelplayerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Whitelistsingelplayer.setClientName(
            MinecraftClient.getInstance().getSession().getUsername()
        );
    }
}
