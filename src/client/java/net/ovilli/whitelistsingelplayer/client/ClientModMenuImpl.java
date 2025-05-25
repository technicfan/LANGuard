package net.ovilli.whitelistsingelplayer.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;
import net.ovilli.whitelistsingelplayer.client.config.WhitelistConfigScreen;

public class ClientModMenuImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<Screen> getModConfigScreenFactory() {
        return parent -> WhitelistConfigScreen.getScreen(parent);
    }
}
