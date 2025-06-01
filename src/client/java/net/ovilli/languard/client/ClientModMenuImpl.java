package net.ovilli.languard.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;
import net.ovilli.languard.client.config.LANGuardConfigScreen;


public class ClientModMenuImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<Screen> getModConfigScreenFactory() {
        return parent -> LANGuardConfigScreen.getScreen(parent);
    }
}