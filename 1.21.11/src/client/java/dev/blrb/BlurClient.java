package dev.blrb;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;

public class BlurClient implements ClientModInitializer {
	public void onInitializeClient() {
		Cfg.ld();
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
