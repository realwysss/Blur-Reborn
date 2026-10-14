package dev.blrb;

import net.fabricmc.api.ClientModInitializer;

public class BlurClient implements ClientModInitializer {
	public void onInitializeClient() {
		Cfg.ld();
	}
}
