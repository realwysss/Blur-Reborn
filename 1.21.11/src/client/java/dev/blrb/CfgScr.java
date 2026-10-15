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
		cc.addEntry(e.startIntSlider(t("Strength"), c.pct, 0, 100).setDefaultValue(50).setTextGetter(v -> t(v + "%")).setTooltip(t("How long the camera shutter stays open. 0% is off, 50% looks like a normal camera, 100% is very strong. The look stays the same at any fps.")).setSaveConsumer(v -> c.pct = v).build());
		String[] qs = {"Low", "Medium", "High", "Ultra"};
		cc.addEntry(e.startSelector(t("Quality"), qs, qs[c.qual]).setDefaultValue(qs[2]).setTooltip(t("How many samples the blur takes at most. Low is the fastest, Ultra is the smoothest. The blur is drawn at half size so it stays cheap.")).setSaveConsumer(v -> c.qual = java.util.Arrays.asList(qs).indexOf(v)).build());
		cc.addEntry(e.startIntSlider(t("Max Blur Length"), c.lim, 1, 40).setDefaultValue(8).setTextGetter(v -> t(v + "%")).setTooltip(t("The longest streak the blur can make, as a percent of the screen height. Lower keeps fast turns readable and cheaper.")).setSaveConsumer(v -> c.lim = v).build());
		cc.addEntry(e.startIntSlider(t("Clear Center"), c.ctr, 0, 100).setDefaultValue(0).setTextGetter(v -> t(v + "%")).setTooltip(t("Keeps the middle of the screen sharper and blurs the edges more, like a lens. 0% blurs everything the same.")).setSaveConsumer(v -> c.ctr = v).build());
		cc.addEntry(e.startBooleanToggle(t("Blur When Moving"), c.mv).setDefaultValue(false).setTooltip(t("Also blurs by distance when you walk or strafe, not only when you turn. Costs a little more.")).setSaveConsumer(v -> c.mv = v).build());
		return b.build();
	}
}