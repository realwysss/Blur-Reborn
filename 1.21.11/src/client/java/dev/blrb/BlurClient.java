package dev.blrb;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;

public class BlurClient implements ClientModInitializer {
	public static KeyMapping key;

	public void onInitializeClient() {
		Cfg.ld();
		KeyMapping k = key = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.blurreborn.open", InputConstants.KEY_N, KeyMapping.Category.register(Identifier.fromNamespaceAndPath("blurreborn", "main"))));
		ClientTickEvents.END_CLIENT_TICK.register(m -> {
			while (k.consumeClick()) if (m.screen == null) {
				if (FabricLoader.getInstance().isModLoaded("wysslib")) m.setScreen(Gui.open(null));
				else if (m.player != null) m.player.displayClientMessage(Component.literal("Blur Reborn needs Wysss Library to open its menu"), true);
			}
		});
		ClientCommandRegistrationCallback.EVENT.register((d, r) -> d.register(ClientCommandManager.literal("motionblur")
				.executes(x -> {
					x.getSource().sendFeedback(Component.literal("motion blur is " + (Cfg.on() ? Cfg.c.pct + "%" : "off")));
					return 1;
				})
				.then(ClientCommandManager.argument("pct", IntegerArgumentType.integer(0, 100)).executes(x -> {
					Cfg.c.pct = IntegerArgumentType.getInteger(x, "pct");
					Cfg.sv();
					x.getSource().sendFeedback(Component.literal(Cfg.c.pct == 0 ? "motion blur off" : "motion blur set to " + Cfg.c.pct + "%"));
					return 1;
				}))));
	}
}
