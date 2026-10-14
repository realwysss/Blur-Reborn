package dev.blrb;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CfgScr {
	static Component t(String s) {
		return Component.literal(s);
	}

	public static Screen mk(Screen p) {
		ConfigBuilder b = ConfigBuilder.create().setParentScreen(p).setTitle(t("Blur Reborn")).setSavingRunnable(Cfg::sv);
		ConfigCategory cc = b.getOrCreateCategory(t("Settings"));
		ConfigEntryBuilder e = b.entryBuilder();
		Cfg c = Cfg.c;
		cc.addEntry(e.startBooleanToggle(t("Motion Blur On"), c.on).setDefaultValue(true).setTooltip(t("Turn this off to remove the motion blur completely. It then costs nothing.")).setSaveConsumer(v -> c.on = v).build());
		String[] ms = {"Smooth (blends frames)", "Turn (blurs when you turn)", "Depth (old, blurs by distance)"};
		cc.addEntry(e.startSelector(t("Mode"), ms, ms[c.mode]).setDefaultValue(ms[1]).setTooltip(t("Smooth blends each frame with the one before it, so everything blurs, also moving mobs and particles. Turn blurs the whole picture when you turn the camera. Depth is the old one that also blurs by distance when you walk. Your hand and the interface stay sharp in all of them.")).setSaveConsumer(v -> c.mode = java.util.Arrays.asList(ms).indexOf(v)).build());
		cc.addEntry(e.startIntSlider(t("Strength"), c.pct, 0, 100).setDefaultValue(50).setTextGetter(v -> t(v + "%")).setTooltip(t("How strong the blur is. 0% is off, 50% looks like a normal camera, 100% is very strong. The look stays the same at any fps.")).setSaveConsumer(v -> c.pct = v).build());
		return b.build();
	}
}