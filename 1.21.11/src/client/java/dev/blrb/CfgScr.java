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
		cc.addEntry(e.startTextDescription(t("Smooths out fast camera movement. It looks at how far every pixel moved since the last frame, so turning and walking both blur correctly. Your hand and the interface stay sharp.")).build());
		cc.addEntry(e.startIntSlider(t("Strength"), c.pct, 0, 100).setDefaultValue(50).setTextGetter(v -> t(v + "%")).setTooltip(t("How long the blur trails are. 0% is off, 50% looks like a normal camera, 100% is very strong. The look stays the same at any fps.")).setSaveConsumer(v -> c.pct = v).build());
		return b.build();
	}
}