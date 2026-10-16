package dev.blrb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public class Cfg {
	public boolean on = true;
	public int pct = 50;
	public boolean mv = false;
	public int qual = 2;
	public int lim = 20;
	public int ver = 2;
	public int ctr = 0;

	static final Gson G = new GsonBuilder().setPrettyPrinting().create();
	static final Path P = FabricLoader.getInstance().getConfigDir().resolve("blurreborn.json");
	public static Cfg c = new Cfg();

	public static void ld() {
		try {
			if (Files.exists(P)) {
				Cfg n = G.fromJson(Files.readString(P), Cfg.class);
				if (n != null) c = n;
			}
		} catch (Exception e) {
			c = new Cfg();
		}
		if (c.ver < 2) {
			c.ver = 2;
			c.lim = Math.max(c.lim, 20);
		}
		c.pct = Math.max(0, Math.min(100, c.pct));
		c.qual = Math.max(0, Math.min(3, c.qual));
		c.lim = Math.max(1, Math.min(50, c.lim));
		c.ctr = Math.max(0, Math.min(100, c.ctr));
	}

	public static void sv() {
		c.pct = Math.max(0, Math.min(100, c.pct));
		c.qual = Math.max(0, Math.min(3, c.qual));
		c.lim = Math.max(1, Math.min(50, c.lim));
		c.ctr = Math.max(0, Math.min(100, c.ctr));
		try {
			Files.writeString(P, G.toJson(c));
		} catch (Exception e) {
		}
	}

	public static boolean on() {
		return c.on && c.pct > 0;
	}

	public static float exp() {
		return c.pct / 100f * 0.1f;
	}

	public static float cap() {
		return c.lim / 100f;
	}

	public static float smp() {
		return new float[] {8f, 12f, 16f, 24f}[c.qual];
	}
}