package dev.blrb;

import dev.wysl.Wl;
import dev.wysl.ui.Frm;
import net.minecraft.client.gui.screens.Screen;

public class Gui {
	static final String[] QS = {"Low", "Medium", "High", "Ultra"};

	public static Screen open(Screen p) {
		Cfg c = Cfg.c;
		Frm f = Frm.of("Blur Reborn", Cfg::sv)
				.tab("Blur")
				.tgl("Motion blur on", () -> c.on, v -> c.on = v)
				.sld("Strength", () -> c.pct, v -> c.pct = v, 0, 100, "%").tip("How long the camera shutter stays open. 50% looks like a normal camera, 100% is very strong. Looks the same at any fps.")
				.sld("Max blur length", () -> c.lim, v -> c.lim = v, 1, 50, "%").tip("The longest streak the blur can make, as a percent of the screen height.")
				.cho("Quality", QS, () -> c.qual, v -> c.qual = v).tip("How many samples the blur takes. The blur is drawn at half size so it stays cheap.")
				.tab("Extra")
				.sld("Clear center", () -> c.ctr, v -> c.ctr = v, 0, 100, "%").tip("Keeps the middle of the screen sharper and blurs the edges more, like a lens.")
				.tgl("Blur when moving", () -> c.mv, v -> c.mv = v).tip("Also blurs by distance when you walk or strafe (first person only).")
				.note("Command: /motionblur 0 to 100")
				.pre((g, fm, x, y, w, h) -> {
					g.text("Preview", x + 10, y + 9, 0x99FFFFFF);
					float k = c.on ? c.pct / 100f : 0f;
					float t = (float) ((System.nanoTime() / 1.0e9) * 1.4 % 1.0);
					int n = 6;
					float bw = (w - 20) / n;
					for (int i = 0; i < n; i++) {
						int col = new int[]{0xE02020, 0xFF8000, 0xFFE020, 0x20B020, 0x2060FF, 0x9020E0}[i];
						float bx = x + 10 + i * bw + (float) Math.sin(t * 6.2832f) * 8f;
						int steps = 1 + (int) (k * 7);
						for (int s = 0; s < steps; s++) {
							float off = (s - steps / 2f) * (k * 3f);
							int a = Math.max(36, 255 / steps);
							g.fill(bx + off, y + 30, bw - 4, h - 60, 4, a << 24 | col);
						}
					}
				});
		return Wl.screen(p, f, "Blur Reborn", BlurClient.key);
	}
}
