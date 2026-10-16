package dev.blrb;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.network.chat.Component;

public class ModmenuIntgr implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return p -> FabricLoader.getInstance().isModLoaded("wysslib") ? Gui.open(p) : new AlertScreen(() -> Minecraft.getInstance().setScreen(p), Component.literal("Blur Reborn"), Component.literal("Install Wysss Library to open this menu."));
	}
}
