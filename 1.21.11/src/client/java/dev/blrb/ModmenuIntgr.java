package dev.blrb;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class ModmenuIntgr implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return p -> FabricLoader.getInstance().isModLoaded("cloth-config") ? CfgScr.mk(p) : null;
	}
}