package net.ovilli.languard.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.ovilli.languard.LANGuard;

public class LANGuardClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Set client name here to add local player to whitelist
        LANGuard.setClientName(
                MinecraftClient.getInstance().getSession().getUsername()
        );
    }
}
