package dev.blrb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public class Cfg {
	public boolean on = true;
	public int pct = 50;
	public int mode = 1;

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
		c.pct = Math.max(0, Math.min(100, c.pct));
		c.mode = Math.max(0, Math.min(2, c.mode));
	}

	public static void sv() {
		c.pct = Math.max(0, Math.min(100, c.pct));
		c.mode = Math.max(0, Math.min(2, c.mode));
		try {
			Files.writeString(P, G.toJson(c));
		} catch (Exception e) {
		}
	}

	public static boolean on() {
		return c.on && c.pct > 0;
	}

	public static float exp() {
		return c.pct / 100f / 30f;
	}

	public static float keep(float dt) {
		return (float) Math.exp(-dt / (0.05f * c.pct / 100f));
	}

	public static float cap() {
		return 0.03f + 0.12f * c.pct / 100f;
	}
}